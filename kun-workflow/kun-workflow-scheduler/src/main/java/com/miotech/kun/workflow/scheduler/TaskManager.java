package com.miotech.kun.workflow.scheduler;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.miotech.kun.commons.utils.EventConsumer;
import com.miotech.kun.commons.utils.EventLoop;
import com.miotech.kun.commons.utils.Props;
import com.miotech.kun.workflow.common.taskrun.bo.TaskAttemptProps;
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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

@Singleton
public class TaskManager {
    private static final Logger logger = LoggerFactory.getLogger(TaskManager.class);

    private final Executor executor;

    private final TaskRunDao taskRunDao;

    private final EventBus eventBus;

    private final Props props;

    private InnerEventLoop eventLoop;

    private final Map<Long, Boolean> rerunningTaskRunIds = new ConcurrentHashMap<>();


    @Inject
    public TaskManager(Executor executor, TaskRunDao taskRunDao, EventBus eventBus, Props props) {
        this.executor = executor;
        this.taskRunDao = taskRunDao;
        this.props = props;

        this.eventLoop = new InnerEventLoop();
        eventLoop.start();

        this.eventBus = eventBus;
        this.eventBus.register(this.eventLoop);
    }

    /* ----------- public methods ------------ */

    public void submit(List<TaskRun> taskRuns) {
        // 生成对应的TaskAttempt
        List<TaskAttempt> taskAttempts = taskRuns.stream()
                .map(this::createTaskAttempt).collect(Collectors.toList());
        logger.debug("TaskAttempts saved. total={}", taskAttempts.size());
        save(taskAttempts);
        submitSatisfyTaskAttemptToExecutor();
    }

    /**
     * taskRun status must be finished
     *
     * @param taskRun
     */
    public boolean retry(TaskRun taskRun) {
        checkState(taskRun.getStatus().isFinished(), "taskRun status must be finished ");
        // Does the same re-run request invoked in another threads?
        if (rerunningTaskRunIds.put(taskRun.getId(), Boolean.TRUE) != null) {
            logger.warn("Cannot rerun taskrun instance with id = {}. Reason: another thread is attempting to re-run the same task run.", taskRun.getId());
            return false;
        }
        try {
            TaskAttempt taskAttempt = createTaskAttempt(taskRun);
            logger.info("save rerun taskAttempt, taskAttemptId = {}, attempt = {}", taskAttempt.getId(), taskAttempt.getAttempt());
            save(Arrays.asList(taskAttempt));
            updateDownStreamStatus(taskRun.getId(), TaskRunStatus.CREATED);
            submitSatisfyTaskAttemptToExecutor();
            return true;
        } catch (Exception e) {
            logger.error("Failed to re-run taskrun with id = {} due to exceptions.", taskRun.getId());
            throw e;
        } finally {
            // release the lock
            rerunningTaskRunIds.remove(taskRun.getId());
        }
    }

    /* ----------- private methods ------------ */

    private TaskAttempt createTaskAttempt(TaskRun taskRun) {
        checkNotNull(taskRun, "taskRun should not be null.");
        checkNotNull(taskRun.getId(), "taskRun's id should not be null.");

        TaskAttemptProps savedTaskAttempt = taskRunDao.fetchLatestTaskAttempt(taskRun.getId());

        int attempt = 1;
        if (savedTaskAttempt != null) {
            attempt = savedTaskAttempt.getAttempt() + 1;
        }
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
                    if (currentStatus.isSuccess()) {
                        submitSatisfyTaskAttemptToExecutor();
                    } else if (currentStatus.isFailure()) {
                        updateDownStreamStatus(taskAttemptStatusChangeEvent.getTaskRunId(), TaskRunStatus.UPSTREAM_FAILED);
                    }
                }
            }
        }
    }

    private void submitSatisfyTaskAttemptToExecutor() {
        List<TaskAttempt> taskAttemptList = taskRunDao.fetchAllSatisfyTaskAttempt();
        logger.debug("fetch satisfy taskAttempt size = {}", taskAttemptList.size());
        for (TaskAttempt taskAttempt : taskAttemptList) {
            executor.submit(taskAttempt);
        }
    }

    private void updateDownStreamStatus(Long taskRunId, TaskRunStatus taskRunStatus) {
        boolean usePostgres = postgresEnable();
        List<Long> downStreamTaskRunIds = taskRunDao.fetchDownStreamTaskRunIdsRecursive(taskRunId, usePostgres);
        logger.debug("fetch downStream taskRunIds = {},taskRunId = {}", downStreamTaskRunIds, taskRunId);
        taskRunDao.updateAttemptStatusByTaskRunIds(downStreamTaskRunIds, taskRunStatus);
    }

    private boolean postgresEnable(){
        String datasourceUrl = props.getString("datasource.jdbcUrl","");
        return datasourceUrl.contains("postgres");
    }

}
