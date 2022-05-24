package com.miotech.kun.workflow;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Singleton;
import com.miotech.kun.commons.utils.InitializingBean;
import com.miotech.kun.metadata.core.model.dataset.DataStore;
import com.miotech.kun.metadata.core.model.dataset.Dataset;
import com.miotech.kun.workflow.common.lineage.service.LineageService;
import com.miotech.kun.workflow.common.taskrun.dao.TaskRunDao;
import com.miotech.kun.workflow.common.taskrun.state.*;
import com.miotech.kun.workflow.core.event.TaskAttemptCheckEvent;
import com.miotech.kun.workflow.core.event.TaskAttemptFinishedEvent;
import com.miotech.kun.workflow.core.event.TaskAttemptStatusChangeEvent;
import com.miotech.kun.workflow.core.event.TaskRunTransitionEvent;
import com.miotech.kun.workflow.core.model.taskrun.TaskAttempt;
import com.miotech.kun.workflow.core.model.taskrun.TaskRun;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunState;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunStatus;
import com.miotech.kun.workflow.utils.DateTimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.time.OffsetDateTime;
import java.util.*;

/**
 * manager taskRun status transition
 */
@Singleton
public class TaskRunStateMachine implements InitializingBean {

    private final Logger logger = LoggerFactory.getLogger(TaskRunStateMachine.class);
    private final Map<TaskRunStatus, TaskRunState> taskRunStateMap = new HashMap<>();

    private final TaskRunDao taskRunDao;
    private final EventBus eventBus;
    private final LineageService lineageService;

    @Inject
    public TaskRunStateMachine(TaskRunDao taskRunDao, EventBus eventBus, LineageService lineageService) {
        this.taskRunDao = taskRunDao;
        this.eventBus = eventBus;
        this.lineageService = lineageService;
        taskRunStateMap.put(TaskRunStatus.CREATED, new TaskRunCreated());
        taskRunStateMap.put(TaskRunStatus.QUEUED, new TaskRunQueued());
        taskRunStateMap.put(TaskRunStatus.BLOCKED, new TaskRunBlocked());
        taskRunStateMap.put(TaskRunStatus.UPSTREAM_FAILED, new TaskRunUpstreamFailed());
        taskRunStateMap.put(TaskRunStatus.RUNNING, new TaskRunRunning());
        taskRunStateMap.put(TaskRunStatus.CHECK, new TaskRunCheck());
        taskRunStateMap.put(TaskRunStatus.CHECK_FAILED, new TaskRunCheckFailed());
        taskRunStateMap.put(TaskRunStatus.FAILED, new TaskRunFailed());
        taskRunStateMap.put(TaskRunStatus.ABORTED, new TaskRunAborted());
        taskRunStateMap.put(TaskRunStatus.ERROR, new TaskRunError());
        taskRunStateMap.put(TaskRunStatus.SUCCESS, new TaskRunSuccess());
    }

    public void start() {
        eventBus.register(this);
    }

    public void handleEvent(TaskRunTransitionEvent taskRunTransitionEvent) {
        Long taskAttemptId = taskRunTransitionEvent.getTaskAttemptId();
        Optional<TaskAttempt> taskAttemptOption = taskRunDao.fetchAttemptById(taskAttemptId);
        if (!taskAttemptOption.isPresent()) {
            logger.warn("taskAttempt = {}, is not exist", taskAttemptId);
            return;
        }

        TaskAttempt taskAttempt = taskAttemptOption.get();
        TaskRunStatus taskRunStatus = taskAttempt.getTaskRun().getStatus();
        TaskRunState taskRunState = taskRunStateMap.get(taskRunStatus);
        preTransition();
        TaskRunStatus nextStatus = taskRunState.doTransition(taskRunTransitionEvent);
        logger.debug("taskRun: {} change status from {} to {} ", taskAttempt.getTaskRun().getId(), taskRunStatus, nextStatus);
        if (nextStatus == null) {
            throw new IllegalStateException("taskRunEvent = " + taskRunTransitionEvent.getType() + "is not expect for status : " + taskRunStatus);
        }
        postTransition(taskAttempt, taskRunStatus, nextStatus);
    }

    protected void preTransition() {
        //do nothing
    }

    protected void postTransition(TaskAttempt taskAttempt, TaskRunStatus currentStatus, TaskRunStatus nextStatus) {
        updateDatabase(taskAttempt, nextStatus);
        sendEvent(taskAttempt, currentStatus, nextStatus);
    }

    private void updateDatabase(TaskAttempt taskAttempt, TaskRunStatus nextStatus) {
        OffsetDateTime queuedAt = null;
        OffsetDateTime startAt = null;
        OffsetDateTime termAt = null;
        OffsetDateTime endAt = null;
        OffsetDateTime now = DateTimeUtils.now();
        if (nextStatus == TaskRunStatus.QUEUED) {
            queuedAt = now;
        } else if (nextStatus.isRunning()) {
            startAt = now;
        } else if (nextStatus.isTermState()) {
            termAt = now;
            if (nextStatus.isFinished()) {
                endAt = now;
            }
        }
        taskRunDao.updateTaskAttemptStatus(taskAttempt.getId(), nextStatus, queuedAt, startAt, endAt, termAt);
    }

    private void sendEvent(TaskAttempt taskAttempt, TaskRunStatus currentStatus, TaskRunStatus nextStatus) {
        sendStatusChangeEvent(taskAttempt, currentStatus, nextStatus);
        sendCheckEvent(taskAttempt, nextStatus);
        sendFinishedEvent(taskAttempt, nextStatus);
    }

    private void sendStatusChangeEvent(TaskAttempt taskAttempt, TaskRunStatus currentStatus, TaskRunStatus nextStatus) {
        Long taskAttemptId = taskAttempt.getId();
        Long taskId = taskAttempt.getTaskId();
        TaskAttemptStatusChangeEvent event = new TaskAttemptStatusChangeEvent(taskAttemptId, currentStatus, nextStatus, taskAttempt.getTaskName(), taskId);
        logger.info("Post taskAttemptStatusChangeEvent. attemptId={}, event={}", taskAttemptId, event);
        //send status change event
        eventBus.post(event);
    }

    private void sendCheckEvent(TaskAttempt taskAttempt, TaskRunStatus nextStatus) {
        if (!nextStatus.isChecking()) {
            return;
        }
        TaskRun taskRun = taskAttempt.getTaskRun();
        List<Long> inDataSetIds = fetchDataSetIdsByDataStore(taskRun.getInlets());
        List<Long> outDataSetIds = fetchDataSetIdsByDataStore(taskRun.getOutlets());
        TaskAttemptCheckEvent event = new TaskAttemptCheckEvent(taskAttempt.getId(), taskRun.getId(), inDataSetIds, outDataSetIds);
        logger.info("Post taskAttemptCheckEvent. attemptId={}, event={}", taskAttempt.getId(), event);
        //send check event;
        eventBus.post(event);
    }

    private void sendFinishedEvent(TaskAttempt taskAttempt, TaskRunStatus nextStatus) {
        if (!nextStatus.isFinished()) {
            return;
        }
        TaskRun taskRun = taskAttempt.getTaskRun();
        TaskAttemptFinishedEvent event = new TaskAttemptFinishedEvent(taskAttempt.getId(), taskAttempt.getTaskId(), taskRun.getId(), nextStatus, taskRun.getInlets(), taskRun.getOutlets());
        logger.info("Post taskAttemptFinishedEvent. attemptId={}, event={}", taskAttempt.getId(), event);
        eventBus.post(event);
    }

    private List<Long> fetchDataSetIdsByDataStore(List<DataStore> dataStoreList) {
        List<Long> dataSetIds = new ArrayList<>();
        try {
            if (dataStoreList != null) {
                for (DataStore dataStore : dataStoreList) {
                    Optional<Dataset> datasetOptional = lineageService.fetchDatasetByDatastore(dataStore);
                    if (datasetOptional.isPresent()) {
                        dataSetIds.add(datasetOptional.get().getGid());
                    }

                }
            }
        } catch (Exception e) {
            logger.warn("fetch dataset by datastore failed", e);
        }
        return dataSetIds;
    }

    @Override
    public void afterPropertiesSet() {
        start();
    }

    @Subscribe
    public void onReceive(TaskRunTransitionEvent event) {
        handleEvent(event);
    }
}
