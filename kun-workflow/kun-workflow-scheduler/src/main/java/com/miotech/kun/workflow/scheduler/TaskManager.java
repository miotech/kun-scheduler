package com.miotech.kun.workflow.scheduler;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.miotech.kun.commons.utils.EventConsumer;
import com.miotech.kun.commons.utils.EventLoop;
import com.miotech.kun.workflow.common.taskrun.dao.TaskRunDao;
import com.miotech.kun.workflow.core.Executor;
import com.miotech.kun.workflow.core.event.Event;
import com.miotech.kun.workflow.core.event.TaskAttemptStatusChangeEvent;
import com.miotech.kun.workflow.core.model.taskrun.TaskAttempt;
import com.miotech.kun.workflow.core.model.taskrun.TaskRun;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunStatus;
import com.miotech.kun.workflow.utils.WorkflowIdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;

@Singleton
public class TaskManager {
    private static final Logger logger = LoggerFactory.getLogger(TaskManager.class);

    private final Executor executor;

    private final TaskRunDao taskRunDao;

    private final EventBus eventBus;

    private InnerEventLoop eventLoop;

    @Inject
    public TaskManager(Executor executor, TaskRunDao taskRunDao, EventBus eventBus) {
        this.executor = executor;
        this.taskRunDao = taskRunDao;

        this.eventLoop = new InnerEventLoop();

        this.eventBus = eventBus;
        this.eventBus.register(this.eventLoop);
    }

    /* ----------- public methods ------------ */

    public void submit(List<TaskRun> taskRuns) {
        // 生成对应的TaskAttempt
        List<TaskAttempt> taskAttempts = taskRuns.stream()
                .map(this::createTaskAttempt).collect(Collectors.toList());

        save(taskAttempts);
        logger.debug("TaskAttempts saved. total={}", taskAttempts.size());
        List<TaskAttempt> taskAttemptList = taskRunDao.fetchAllSatisfyTaskAttempt();
        logger.debug("fetch satisfy taskAttempt size = {}", taskAttemptList.size());
        for (TaskAttempt taskAttempt : taskAttemptList) {
            executor.submit(taskAttempt);
        }

    }

    /* ----------- private methods ------------ */

    private TaskAttempt createTaskAttempt(TaskRun taskRun) {
        checkNotNull(taskRun, "taskRun should not be null.");
        checkNotNull(taskRun.getId(), "taskRun's id should not be null.");

        int attempt = 1;
        TaskAttempt taskAttempt = TaskAttempt.newBuilder()
                .withId(WorkflowIdGenerator.nextTaskAttemptId(taskRun.getId(), attempt))
                .withTaskRun(taskRun)
                .withAttempt(attempt)
                .withStatus(TaskRunStatus.CREATED)
                .withQueueName(taskRun.getQueueName())
                .withPriority(taskRun.getPriority())
                .build();
        logger.debug("Created taskAttempt. taskAttempt={}", taskAttempt);

        return taskAttempt;
    }

    private void save(List<TaskAttempt> taskAttempts) {
        for (TaskAttempt ta : taskAttempts) {
            taskRunDao.createAttempt(ta);
            taskRunDao.updateTaskAttemptStatus(ta.getId(), TaskRunStatus.CREATED);
        }
    }

    private class InnerEventLoop extends EventLoop<Long, Event> {
        public InnerEventLoop() {
            super("task-manager");
            addConsumers(Arrays.asList(new StatusChangeEventConsumer()));
        }

        @Subscribe
        public void onReceive(TaskAttemptStatusChangeEvent event) {
            post(event.getAttemptId(), event);
        }
    }

    private class StatusChangeEventConsumer extends EventConsumer<Long, Event> {
        @Override
        public void onReceive(Event event) {
            if (event instanceof TaskAttemptStatusChangeEvent) {
                TaskAttemptStatusChangeEvent taskAttemptStatusChangeEvent = (TaskAttemptStatusChangeEvent) event;
                TaskRunStatus currentStatus = taskAttemptStatusChangeEvent.getToStatus();
                if (currentStatus.isFinished()) {
                    List<TaskAttempt> satisfyTaskAttempts = taskRunDao.fetchAllSatisfyTaskAttempt();
                    logger.info("invoke downStream task attempt size = {}", satisfyTaskAttempts.size());
                    for (TaskAttempt taskAttempt : satisfyTaskAttempts) {
                        executor.submit(taskAttempt);
                    }
                }
            }
        }
    }

}
