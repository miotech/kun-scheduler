package com.miotech.kun.workflow.scheduler;

import com.google.common.collect.Lists;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.miotech.kun.commons.utils.EventConsumer;
import com.miotech.kun.commons.utils.EventLoop;
import com.miotech.kun.workflow.common.graph.DirectTaskGraph;
import com.miotech.kun.workflow.common.operator.service.OperatorService;
import com.miotech.kun.workflow.common.taskrun.dao.TaskRunDao;
import com.miotech.kun.workflow.common.tick.TickDao;
import com.miotech.kun.workflow.common.variable.service.VariableService;
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
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
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

    private final VariableService variableService;

    private final EventBus eventBus;

    private final TickDao tickDao;

    private final long SCHEDULE_INTERVAL = 60000l;

    private final Deque<TaskGraph> graphs;
    private final InnerEventLoop eventLoop;

    @Inject
    public TaskSpawner(TaskManager taskManager,
                       TaskRunDao taskRunDao,
                       OperatorService operatorService,
                       VariableService variableService,
                       EventBus eventBus,
                       TickDao tickDao) {
        this.taskManager = taskManager;
        this.taskRunDao = taskRunDao;
        this.operatorService = operatorService;
        this.variableService = variableService;
        this.eventBus = eventBus;

        this.graphs = new ConcurrentLinkedDeque<>();
        this.eventLoop = new InnerEventLoop();
        this.eventBus.register(this.eventLoop);
        this.tickDao = tickDao;
        this.eventLoop.start();
        init();
    }

    /* ----------- public methods ------------ */

    public void schedule(TaskGraph graph) {
        checkNotNull(graph, "graph should not be null.");
        graphs.add(graph);
    }

    public void init(){
        List<TaskRun> unStartedTaskRunList = taskRunDao.fetchUnStartedTaskRunList();
        submit(unStartedTaskRunList);
        logger.info("submit unStartedTaskRun = {}", unStartedTaskRunList);
    }

    public List<TaskRun> run(TaskGraph graph) {
        return run(graph, TaskRunEnv.EMPTY);
    }

    public List<TaskRun> run(TaskGraph graph, Tick tick) {
        checkNotNull(graph, "graph should not be null.");
        return spawn(Lists.newArrayList(graph), tick, TaskRunEnv.EMPTY);
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
        logger.debug("handle TickEvent tickEvent={}", tickEvent);
        spawn(graphs, tickEvent.getTick(), TaskRunEnv.EMPTY);
    }

    private List<TaskRun> spawn(Collection<TaskGraph> graphs, Tick tick, TaskRunEnv env) {
        List<TaskRun> taskRuns = new ArrayList<>();
        Tick checkPoint = tickDao.getLatestCheckPoint();
        if (checkPoint == null) {
            checkPoint = new Tick(tick.toOffsetDateTime().plusMinutes(-1));
        }
        logger.info("checkPoint = {}", checkPoint);
        OffsetDateTime checkpointTime = checkPoint.toOffsetDateTime();
        OffsetDateTime currentTickTime = tick.toOffsetDateTime();
        while (checkpointTime.compareTo(currentTickTime) < 0) {
            List<TaskRun> recoverTaskRun = new ArrayList<>();
            checkpointTime = checkpointTime.plus(SCHEDULE_INTERVAL, ChronoUnit.MILLIS);
            OffsetDateTime scheduleTime = checkpointTime.compareTo(currentTickTime) < 0 ?
                    checkpointTime : currentTickTime;
            Tick recoverTick = new Tick(scheduleTime);
            Map<TaskGraph, List<Task>> graphTasks = new HashMap<>();
            for (TaskGraph graph : graphs) {
                List<Task> tasksToRun = graph.tasksScheduledAt(tick).stream().
                        filter(task -> task.shouldSchedule(scheduleTime, currentTickTime)).collect(Collectors.toList());
                logger.debug("tasks to run: {}, at tick {}", tasksToRun, recoverTick);
                recoverTaskRun.addAll(createTaskRuns(tasksToRun, recoverTick, env));
                graphTasks.put(graph, tasksToRun);

            }
            logger.debug("to save created TaskRuns. TaskRuns={}", recoverTaskRun);
            save(recoverTaskRun);
            updateGraphsTask(graphTasks, recoverTick);
            logger.debug("to save checkpoint. checkpoint = {}", recoverTick);
            tickDao.saveCheckPoint(recoverTick);
            taskRuns.addAll(recoverTaskRun);
        }

        logger.debug("to submit created TaskRuns. TaskRuns={}", taskRuns);
        submit(taskRuns);
        return taskRuns;
    }

    private void updateGraphsTask(Map<TaskGraph, List<Task>> graphTasks, Tick tick) {
        for (Map.Entry<TaskGraph, List<Task>> entry : graphTasks.entrySet()) {
            TaskGraph graph = entry.getKey();
            logger.debug("to update graph. graph = {} , tasks = {}", graph, entry.getValue());
            graph.updateTasksNextExecutionTick(tick, entry.getValue());
        }
    }

    //幂等，重放tick不会创建新的taskRun
    private List<TaskRun> createTaskRuns(List<Task> tasks, Tick tick, TaskRunEnv env) {
        List<TaskRun> results = new ArrayList<>(tasks.size());
        for (Task task : tasks) {
            TaskRun taskRun = taskRunDao.fetchTaskRunByTaskAndTick(task.getId(), tick);
            if (taskRun == null) {
                results.add(createTaskRun(task, tick, env.getConfig(task.getId()), results));
            }
        }
        return results;
    }

    private TaskRun createTaskRun(Task task, Tick tick, Map<String, Object> runtimeConfig, List<TaskRun> others) {
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

    private Config prepareConfig(Task task, Config config, Map<String, Object> runtimeConfig) {
        ConfigDef configDef = operatorService.getOperatorConfigDef(task.getOperatorId());

        // populate default values
        config = new Config(configDef, config.getValues());

        // override by runtime config
        Config rtConfig = new Config(runtimeConfig);
        rtConfig.validateBy(configDef);
        Config mergedConfig = config.overrideBy(rtConfig);

        // render variables
        Config finalConfig = variableService.renderConfig(mergedConfig);

        // validate final config
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
