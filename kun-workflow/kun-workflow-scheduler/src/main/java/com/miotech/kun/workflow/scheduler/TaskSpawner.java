package com.miotech.kun.workflow.scheduler;

import com.google.common.collect.Lists;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.miotech.kun.commons.utils.EventConsumer;
import com.miotech.kun.commons.utils.EventLoop;
import com.miotech.kun.workflow.common.graph.DirectTaskGraph;
import com.miotech.kun.workflow.common.operator.service.OperatorService;
import com.miotech.kun.workflow.common.taskrun.dao.TaskRunDao;
import com.miotech.kun.workflow.core.event.Event;
import com.miotech.kun.workflow.core.event.TickEvent;
import com.miotech.kun.workflow.core.execution.Config;
import com.miotech.kun.workflow.core.execution.ConfigDef;
import com.miotech.kun.workflow.core.model.common.Tick;
import com.miotech.kun.workflow.core.model.task.DependencyFunction;
import com.miotech.kun.workflow.core.model.task.Task;
import com.miotech.kun.workflow.core.model.task.TaskGraph;
import com.miotech.kun.workflow.core.model.task.TaskRunEnv;
import com.miotech.kun.workflow.core.model.taskrun.TaskRun;
import com.miotech.kun.workflow.utils.DateTimeUtils;
import com.miotech.kun.workflow.utils.WorkflowIdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static java.lang.String.format;

@Singleton
public class TaskSpawner {
    private static final Logger logger = LoggerFactory.getLogger(TaskSpawner.class);

    private final TaskManager taskManager;

    private final TaskRunDao taskRunDao;

    private final OperatorService operatorService;

    private final EventBus eventBus;

    private final Deque<TaskGraph> graphs;
    private final InnerEventLoop eventLoop;

    @Inject
    public TaskSpawner(TaskManager taskManager, TaskRunDao taskRunDao, OperatorService operatorService, EventBus eventBus) {
        this.taskManager = taskManager;
        this.taskRunDao = taskRunDao;
        this.operatorService = operatorService;
        this.eventBus = eventBus;

        this.graphs = new ConcurrentLinkedDeque<>();
        this.eventLoop = new InnerEventLoop();
        this.eventBus.register(this.eventLoop);
        this.eventLoop.start();
    }

    /* ----------- public methods ------------ */

    public void schedule(TaskGraph graph) {
        checkNotNull(graph, "graph should not be null.");
        graphs.add(graph);
    }

    public List<TaskRun> run(TaskGraph graph) {
        return run(graph, TaskRunEnv.EMPTY);
    }

    public List<TaskRun> run(TaskGraph graph, TaskRunEnv env) {
        checkNotNull(graph, "graph should not be null.");
        checkNotNull(env, "env should not be null.");
        checkState(graph instanceof DirectTaskGraph, "Only DirectTaskGraph is accepted.");

        Tick current = new Tick(DateTimeUtils.now());
        return spawn(Lists.newArrayList(graph), current, env);
    }

    /* ----------- private methods ------------ */

    private void handleTickEvent(TickEvent tickEvent) {
        logger.debug("handle TickEvent. event={}", tickEvent);
        spawn(graphs, tickEvent.getTick(), TaskRunEnv.EMPTY);
    }

    private List<TaskRun> spawn(Collection<TaskGraph> graphs, Tick tick, TaskRunEnv env) {
        List<TaskRun> taskRuns = new ArrayList<>();
        for (TaskGraph graph : graphs) {
            List<Task> tasksToRun = graph.tasksScheduledAt(tick);
            logger.debug("tasks to run: {}, at tick {}", tasksToRun, tick);
            taskRuns.addAll(createTaskRuns(tasksToRun, tick, env));
        }

        logger.debug("to save created TaskRuns. TaskRuns={}", taskRuns);
        save(taskRuns);

        logger.debug("to submit created TaskRuns. TaskRuns={}", taskRuns);
        submit(taskRuns);

        return taskRuns;
    }

    private List<TaskRun> createTaskRuns(List<Task> tasks, Tick tick, TaskRunEnv env) {
        List<TaskRun> results = new ArrayList<>(tasks.size());
        for (Task task : tasks) {
            results.add(createTaskRun(task, tick, env.getConfig(task.getId()), results));
        }
        return results;
    }

    private TaskRun createTaskRun(Task task, Tick tick, Map<String, String> runtimeConfig, List<TaskRun> others) {
        TaskRun taskRun = TaskRun.newBuilder()
                .withId(WorkflowIdGenerator.nextTaskRunId())
                .withTask(task)
                .withConfig(prepareConfig(task, task.getConfig(), runtimeConfig))
                .withScheduledTick(tick)
                .withDependentTaskRunIds(resolveDependencies(task, tick, others))
                .build();
        logger.debug("TaskRun is created successfully TaskRun={}, Task={}, Tick={}.", taskRun, task, tick);
        return taskRun;
    }

    private List<Long> resolveDependencies(Task task, Tick tick, List<TaskRun> others) {
        return task.getDependencies().stream()
                .flatMap(dependency -> {
                    DependencyFunction depFunc = dependency.getDependencyFunction();
                    return depFunc.resolveDependency(task, dependency.getUpstreamTaskId(), tick, others)
                            .stream();
                }).collect(Collectors.toList());
    }

    private Config prepareConfig(Task task, Config defaultConfig, Map<String, String> runtimeConfig) {
        ConfigDef configDef = operatorService.getOperatorConfigDef(task.getOperatorId());
        Config rtConfig = new Config(configDef, runtimeConfig);
        Config finalConfig = defaultConfig.overrideBy(rtConfig);
        validateConfig(configDef, finalConfig, rtConfig);
        return finalConfig;
    }

    private void validateConfig(ConfigDef configDef, Config finalConfig, Config runtimeConfig) {
        for (ConfigDef.ConfigKey configKey : configDef.configKeys()) {
            String name = configKey.getName();

            if (configKey.isRequired() && !finalConfig.contains(name)) {
                throw new IllegalArgumentException(format("Configuration %s is required but not specified", name));
            }

            if (!configKey.isReconfigurable() && runtimeConfig.contains(name)) {
                throw new IllegalArgumentException(format("Configuration %s should not be reconfigured.", name));
            }
        }
    }

    private void save(List<TaskRun> taskRuns) {
        taskRunDao.createTaskRuns(taskRuns);
    }

    private void submit(List<TaskRun> taskRuns) {
        taskManager.submit(taskRuns);
    }

    private class InnerEventLoop extends EventLoop<Long, Event> {
        public InnerEventLoop() {
            super("task-spawner");
            addConsumers(Lists.newArrayList(
                    new EventConsumer<Long, Event>() {
                        @Override
                        public void onReceive(Event event) {
                            handleTickEvent((TickEvent) event);
                        }
                    }
            ));
        }

        @Subscribe
        public void onReceive(TickEvent event) {
            post(1L, event);
        }
    }
}
