package com.miotech.kun.workflow.scheduler;

import com.google.common.collect.Lists;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.miotech.kun.commons.utils.EventConsumer;
import com.miotech.kun.commons.utils.EventLoop;
import com.miotech.kun.workflow.common.taskrun.dao.TaskRunDao;
import com.miotech.kun.workflow.core.event.Event;
import com.miotech.kun.workflow.core.event.TickEvent;
import com.miotech.kun.workflow.core.model.common.Tick;
import com.miotech.kun.workflow.core.model.common.Variable;
import com.miotech.kun.workflow.core.model.task.DependencyFunction;
import com.miotech.kun.workflow.core.model.task.Task;
import com.miotech.kun.workflow.core.model.task.TaskGraph;
import com.miotech.kun.workflow.core.model.taskrun.TaskRun;
import com.miotech.kun.workflow.utils.WorkflowIdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;

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

    public void add(TaskGraph graph) {
        checkNotNull(graph, "graph should not be null.");
        graphs.add(graph);
    }

    private void handleTickEvent(TickEvent tickEvent) {
        logger.debug("handle TickEvent. event={}", tickEvent);

        List<TaskRun> taskRuns = new ArrayList<>();
        for (TaskGraph graph : graphs) {
            Tick tick = tickEvent.getTick();
            List<Task> tasksToRun = graph.tasksScheduledAt(tick);
            logger.debug("tasks to run: {}, at tick {}", tasksToRun, tickEvent.getTick());
            taskRuns.addAll(createTaskRuns(tasksToRun, tick));
        }

        logger.debug("to save created TaskRuns. TaskRuns={}", taskRuns);
        save(taskRuns);

        logger.debug("to submit created TaskRuns. TaskRuns={}", taskRuns);
        submit(taskRuns);

        // TODO: 删除不再可能产生新任务的Graph
    }

    private List<TaskRun> createTaskRuns(List<Task> tasks, Tick tick) {
        List<TaskRun> results = new ArrayList<>(tasks.size());
        for (Task task : tasks) {
            results.add(createTaskRun(task, tick, results));
        }
        return results;
    }

    private TaskRun createTaskRun(Task task, Tick tick, List<TaskRun> others) {
        TaskRun taskRun = TaskRun.newBuilder()
                .withId(WorkflowIdGenerator.nextTaskRunId())
                .withTask(task)
                .withVariables(prepareVariables(task.getVariableDefs()))
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

    private List<Variable> prepareVariables(List<Variable> variableDefs) {
        return variableDefs;
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
