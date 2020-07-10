package com.miotech.kun.workflow.scheduler;

import com.google.common.collect.Lists;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.miotech.kun.commons.utils.EventConsumer;
import com.miotech.kun.commons.utils.EventLoop;
import com.miotech.kun.workflow.common.graph.DirectTaskGraph;
import com.miotech.kun.workflow.common.taskrun.dao.TaskRunDao;
import com.miotech.kun.workflow.core.event.Event;
import com.miotech.kun.workflow.core.event.TickEvent;
import com.miotech.kun.workflow.core.model.common.Tick;
import com.miotech.kun.workflow.core.model.common.Variable;
import com.miotech.kun.workflow.core.model.task.DependencyFunction;
import com.miotech.kun.workflow.core.model.task.RunTaskContext;
import com.miotech.kun.workflow.core.model.task.Task;
import com.miotech.kun.workflow.core.model.task.TaskGraph;
import com.miotech.kun.workflow.core.model.taskrun.TaskRun;
import com.miotech.kun.workflow.utils.DateTimeUtils;
import com.miotech.kun.workflow.utils.WorkflowIdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

@Singleton
public class TaskSpawner {
    private static final Logger logger = LoggerFactory.getLogger(TaskSpawner.class);

    private final TaskRunner taskRunner;

    private final TaskRunDao taskRunDao;

    private final EventBus eventBus;

    private final Deque<TaskGraph> graphs;
    private final TaskSpawnerEventLoop eventLoop;

    @Inject
    public TaskSpawner(TaskRunner taskRunner, TaskRunDao taskRunDao, EventBus eventBus) {
        this.taskRunner = taskRunner;
        this.taskRunDao = taskRunDao;
        this.eventBus = eventBus;

        this.graphs = new ConcurrentLinkedDeque<>();
        this.eventLoop = new TaskSpawnerEventLoop();
        this.eventBus.register(this.eventLoop);
        this.eventLoop.start();
    }

    /* ----------- public methods ------------ */

    public void schedule(TaskGraph graph) {
        checkNotNull(graph, "graph should not be null.");
        graphs.add(graph);
    }

    public List<TaskRun> run(TaskGraph graph) {
        return run(graph, RunTaskContext.EMPTY);
    }

    public List<TaskRun> run(TaskGraph graph, RunTaskContext context) {
        checkNotNull(graph, "graph should not be null.");
        checkNotNull(context, "context should not be null.");
        checkState(graph instanceof DirectTaskGraph, "Only DirectTaskGraph is accepted.");

        Tick current = new Tick(DateTimeUtils.now());
        return spawn(Lists.newArrayList(graph), current, context);
    }

    /* ----------- private methods ------------ */

    private void handleTickEvent(TickEvent tickEvent) {
        logger.debug("handle TickEvent. event={}", tickEvent);
        spawn(graphs, tickEvent.getTick(), RunTaskContext.EMPTY);
    }

    private List<TaskRun> spawn(Collection<TaskGraph> graphs, Tick tick, RunTaskContext context) {
        List<TaskRun> taskRuns = new ArrayList<>();
        for (TaskGraph graph : graphs) {
            List<Task> tasksToRun = graph.tasksScheduledAt(tick);
            logger.debug("tasks to run: {}, at tick {}", tasksToRun, tick);
            taskRuns.addAll(createTaskRuns(tasksToRun, tick, context));
        }

        logger.debug("to save created TaskRuns. TaskRuns={}", taskRuns);
        save(taskRuns);

        logger.debug("to submit created TaskRuns. TaskRuns={}", taskRuns);
        submit(taskRuns);

        return taskRuns;
    }

    private List<TaskRun> createTaskRuns(List<Task> tasks, Tick tick, RunTaskContext context) {
        List<TaskRun> results = new ArrayList<>(tasks.size());
        for (Task task : tasks) {
            results.add(createTaskRun(task, tick, context.getConfiguredVariables(task.getId()), results));
        }
        return results;
    }

    private TaskRun createTaskRun(Task task, Tick tick, List<Variable> configuredVariables, List<TaskRun> others) {
        TaskRun taskRun = TaskRun.newBuilder()
                .withId(WorkflowIdGenerator.nextTaskRunId())
                .withTask(task)
                .withVariables(prepareVariables(task.getVariableDefs(), configuredVariables))
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

    private List<Variable> prepareVariables(List<Variable> variableDefs, List<Variable> configuredVariables) {
        Map<String, Variable> lookupTable = configuredVariables.stream()
                .collect(Collectors.toMap(Variable::getKey, Function.identity()));
        return variableDefs.stream().map(vd -> {
            if (lookupTable.containsKey(vd.getKey())) {
                return vd.withValue(lookupTable.get(vd.getKey()).getValue());
            } else {
                return vd;
            }
        }).collect(Collectors.toList());
    }

    private void save(List<TaskRun> taskRuns) {
        taskRunDao.createTaskRuns(taskRuns);
    }

    private void submit(List<TaskRun> taskRuns) {
        taskRunner.submit(taskRuns);
    }

    private class TaskSpawnerEventLoop extends EventLoop<Long, Event> {
        public TaskSpawnerEventLoop() {
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
