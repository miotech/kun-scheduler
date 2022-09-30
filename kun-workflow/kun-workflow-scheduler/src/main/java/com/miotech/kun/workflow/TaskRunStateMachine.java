package com.miotech.kun.workflow;

import com.google.common.collect.ImmutableList;
import com.google.common.eventbus.EventBus;
import com.google.common.util.concurrent.Uninterruptibles;
import com.miotech.kun.metadata.core.model.dataset.DataStore;
import com.miotech.kun.metadata.core.model.dataset.Dataset;
import com.miotech.kun.workflow.common.lineage.service.LineageService;
import com.miotech.kun.workflow.common.taskrun.dao.TaskRunDao;
import com.miotech.kun.workflow.core.event.*;
import com.miotech.kun.workflow.core.model.taskrun.*;
import com.miotech.kun.workflow.utils.DateTimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.miotech.kun.workflow.core.event.TaskRunTransitionEventType.*;

public class TaskRunStateMachine {

    private final Logger logger = LoggerFactory.getLogger(TaskRunStateMachine.class);

    private final Long taskRunId;
    private Long taskAttemptId;
    private OffsetDateTime queuedAt = null;
    private OffsetDateTime startAt = null;
    private OffsetDateTime termAt = null;
    private OffsetDateTime endAt = null;
    private final Map<Integer, TaskRunState> taskRunStateMap;
    private final EventBus eventBus;
    private final LineageService lineageService;
    private final TaskRunDao taskRunDao;
    private Integer taskAttemptPhase;
    private AtomicInteger status = new AtomicInteger(0);
    private Queue<TaskRunTransitionEvent> eventQueue = new LinkedList<>();
    private List<TaskRunTransitionEvent> finishEvents = new ArrayList<>();
    private ConditionManager conditionManager;
    private final Integer IDLE = 0;
    private final Integer SCHEDULED = 1;
    private final Integer CLOSED = 2;
    private final Integer SHOULD_SCHEDULE_MASK = 3;


    public TaskRunStateMachine(TaskAttempt taskAttempt, Map<Integer, TaskRunState> taskRunStateMap,
                               EventBus eventBus, LineageService lineageService, TaskRunDao taskRunDao) {
        this.taskRunId = taskAttempt.getTaskRun().getId();
        this.taskAttemptId = taskAttempt.getId();
        this.taskRunStateMap = taskRunStateMap;
        this.eventBus = eventBus;
        this.lineageService = lineageService;
        this.taskRunDao = taskRunDao;
        this.taskAttemptPhase = taskAttempt.getTaskRunPhase();

    }

    public void generateCondition() {
        logger.debug("generate condition for taskRun : {}", taskRunId);
        try {
            List<TaskRunCondition> conditions = taskRunDao.fetchTaskRunConditionsById(taskRunId);
            Map<Long, TaskRunCondition> conditionMap = new HashMap<>();
            for (TaskRunCondition taskRunCondition : conditions) {
                Long upTaskRunId = Long.valueOf(taskRunCondition.getCondition().getContent().get("taskRunId"));
                conditionMap.put(upTaskRunId, taskRunCondition);
            }
            List<Long> conditionTaskRunIds = conditionMap.keySet().stream().collect(Collectors.toList());
            conditionManager = new ConditionManager(conditionMap);

            List<Optional<TaskRun>> conditionTaskRuns = taskRunDao.fetchTaskRunsByIds(conditionTaskRunIds);
            for (Optional<TaskRun> conditionTaskRun : conditionTaskRuns) {
                if (conditionTaskRun.isPresent()) {
                    initCondition(conditionTaskRun.get().getId(), conditionTaskRun.get().getTaskRunPhase());
                }
            }
        } catch (Exception e) {
            logger.error("generate condition for taskRunId failed", taskRunId, e);
        }


    }

    public void doTransition() {

        while (!eventQueue.isEmpty()) {
            if (eventQueue.isEmpty()) {
                Uninterruptibles.sleepUninterruptibly(1, TimeUnit.SECONDS);
                return;
            }
            TaskRunTransitionEvent nextEvent = eventQueue.poll();
            try {
                Integer currentPhase = TaskRunPhase.getCurrentPhase(taskAttemptPhase);
                logger.debug("taskAttempt : {} going to doTransition for event {}, currentPhase is {}", taskAttemptId, nextEvent, currentPhase);
                TaskRunState taskRunState = taskRunStateMap.get(currentPhase);
                TaskAttempt taskAttempt = taskRunDao.fetchAttemptById(taskAttemptId).get();
                TaskRunSMMessage message = new TaskRunSMMessage(nextEvent, taskAttempt);
                TaskRunState nextState = taskRunState.doTransition(message);
                if (nextState == null) {
                    throw new IllegalStateException("taskRunEvent = " + nextEvent.getType() + " is not expect for state : " + taskRunState);
                }
                if (nextEvent.getType().equals(RESCHEDULE)) {
                    resetStateMachine();
                }
                if (nextEvent.getType().equals(ASSEMBLED)) {
                    termAt = null;
                }
                Integer prePhase = taskAttemptPhase;
                taskAttemptPhase = computePhase(nextState.getPhase());
                logger.debug("taskAttempt: {} change state from {} to {} ", taskAttemptId, taskRunState, nextState);
                postTransition(prePhase, nextEvent);
                taskRunState.afterTransition(nextEvent, taskAttempt);
            } catch (Throwable e) {
                logger.error("attempt {}  " +
                        "do transition failed for phase  {}", taskAttemptId, taskAttemptPhase, e);
            }
            finishEvents.add(nextEvent);
            saveFinishedEvents();

        }
        setIdle();

    }

    public void recover() {
        List<TaskRunTransitionEvent> unCompletedEvents = taskRunDao.fetchUnCompletedEvents(taskAttemptId);
        if (unCompletedEvents.size() > 0) {
            logger.debug("{} recover {} unCompleted events", taskAttemptId, unCompletedEvents.size());
            eventQueue.addAll(unCompletedEvents);
        } else {
            //if not unCompleted event, recover last event
            eventQueue.add(new TaskRunTransitionEvent(RECOVER, taskAttemptId, null));
        }
    }

    public void conditionChange(Long fromTaskRunId, Integer prePhase, Integer nextPhase) {
        TaskRunCondition taskRunCondition = conditionManager.conditionChange(fromTaskRunId, prePhase, nextPhase);
        taskRunDao.updateTaskRunCondition(taskRunId, taskRunCondition);
    }

    public void removeCondition(Long fromTaskRunId, Integer fromTaskRunPhase) {
        conditionManager.removeCondition(fromTaskRunId, fromTaskRunPhase);
    }

    public boolean conditionHasSatisfied() {
        return conditionManager.conditionSatisfy();
    }

    public ConditionStatus resolveCondition() {
        return conditionManager.resolveCondition();
    }

    public boolean isScheduling() {
        return (status.get() & SCHEDULED) != 0;
    }

    public boolean isIdle() {
        return status.get() == 0;
    }

    public boolean isClosed() {
        return (status.get() & CLOSED) != 0;
    }

    public Long getTaskAttemptId() {
        return taskAttemptId;
    }

    public Integer getTaskAttemptPhase() {
        return taskAttemptPhase;
    }

    public Long getTaskRunId() {
        return taskRunId;
    }

    public OffsetDateTime getQueuedAt() {
        return queuedAt;
    }

    public OffsetDateTime getStartAt() {
        return startAt;
    }

    public OffsetDateTime getTermAt() {
        return termAt;
    }

    public OffsetDateTime getEndAt() {
        return endAt;
    }

    public List<TaskRunTransitionEvent> getFinishEvents() {
        return ImmutableList.copyOf(finishEvents);
    }

    /**
     * Set Scheduled status
     *
     * @return true if original status is idle,else return false when status is scheduled or close
     */
    public boolean setSchedule() {
        Integer currentStatus = status.get();
        if (shouldBeSchedule(currentStatus)) {
            Integer nextStatus = currentStatus | SCHEDULED;
            return updateStatus(currentStatus, nextStatus);
        }
        return false;
    }

    /**
     * Set Close status
     *
     * @return
     */
    public boolean setClosed() {
        Integer currentStatus = status.get();
        Integer nextStatus = currentStatus | CLOSED;
        return updateStatus(currentStatus, nextStatus) || setClosed();
    }

    public void receiveEvent(TaskRunTransitionEvent taskRunTransitionEvent) {
        logger.debug("receive event : {}", taskRunTransitionEvent);
        eventQueue.add(taskRunTransitionEvent);
    }

    /**
     * Set Idle status
     *
     * @return
     */
    private boolean setIdle() {
        Integer currentStatus = status.get();
        return updateStatus(currentStatus, IDLE) || setIdle();
    }

    private void initCondition(Long fromTaskRunId, Integer fromTaskRunPhase) {
        TaskRunCondition taskRunCondition = conditionManager.initCondition(fromTaskRunId, fromTaskRunPhase);
        taskRunDao.updateTaskRunCondition(taskRunId, taskRunCondition);
    }

    private boolean updateStatus(Integer currentStatus, Integer nextStatus) {
        return status.compareAndSet(currentStatus, nextStatus);
    }

    private boolean shouldBeSchedule(Integer currentStatus) {
        if ((currentStatus & SHOULD_SCHEDULE_MASK) != IDLE) {
            return false;
        }
        return true;
    }

    private Integer computePhase(Integer nextPhase) {
        if (nextPhase < taskAttemptPhase) {
            Integer mask = nextPhase - 1;
            Integer resetPhase = taskAttemptPhase & mask;
            return resetPhase | nextPhase;
        }
        return taskAttemptPhase | nextPhase;
    }

    private void resetStateMachine() {
        logger.debug("reset state machine for {}", taskRunId);
        queuedAt = null;
        startAt = null;
        termAt = null;
        endAt = null;
        taskAttemptId = taskAttemptId + 1;
    }

    private void postTransition(Integer prePhase, TaskRunTransitionEvent event) {
        TaskRunStatus currentStatus = TaskRunPhase.toStatus(prePhase);
        TaskRunStatus nextStatus = TaskRunPhase.toStatus(taskAttemptPhase);
        logger.debug("fetch taskAttempt by {}", taskAttemptId);
        TaskAttempt taskAttempt = taskRunDao.fetchAttemptById(taskAttemptId).get();

        OffsetDateTime now = DateTimeUtils.now();
        if (nextStatus == TaskRunStatus.QUEUED) {
            queuedAt = now;
        } else if (nextStatus.isRunning()) {
            startAt = now;
        } else if (nextStatus.isTermState()) {
            termAt = now;
            if (nextStatus.isFinished() && !nextStatus.isSkipped()) {
                endAt = now;
            }
        }
        updateDatabase(this, taskAttemptPhase);

        if (!nextStatus.equals(taskAttempt.getStatus())) {
            sendEvent(taskAttempt, currentStatus, nextStatus);
        }

        invokeDownStream(prePhase, event);

    }

    private void saveFinishedEvents() {
        for (TaskRunTransitionEvent finishEvent : getFinishEvents()) {
            taskRunDao.updateTransitEventCompleted(finishEvent);
        }
    }


    private void invokeDownStream(Integer prePhase, TaskRunTransitionEvent event) {
        TaskRunTransitionEventType downStreamEventType = covertDownStreamEvent(prePhase, event.getType());
        if (downStreamEventType != null) {
            List<Long> taskRunIds = taskRunDao.taskRunIdsShouldBeInvokeCondition(taskRunId);
            List<Long> taskAttemptIds = taskRunDao.fetchAllLatestTaskAttemptIds(taskRunIds);
            for (Long attemptId : taskAttemptIds) {
                FromTaskRunContext.Builder taskRunContextBuilder = FromTaskRunContext
                        .newBuilder()
                        .withTaskRunId(taskRunId)
                        .withPrePhase(prePhase)
                        .withTaskRunPhase(taskAttemptPhase);
                FromTaskRunContext fromTaskRunContext = event.getFromTaskRunContext();
                //set root event sender
                if (fromTaskRunContext != null && fromTaskRunContext.getSrcTaskRunId() != null) {
                    taskRunContextBuilder.withSrcTaskRunId(fromTaskRunContext.getSrcTaskRunId());
                } else {
                    taskRunContextBuilder.withSrcTaskRunId(taskRunId);
                }
                TaskRunTransitionEvent downStreamEvent = new TaskRunTransitionEvent(downStreamEventType, attemptId, taskRunContextBuilder.build());
                eventBus.post(downStreamEvent);
            }
        }
    }

    private TaskRunTransitionEventType covertDownStreamEvent(Integer prePhase, TaskRunTransitionEventType eventType) {
        TaskRunTransitionEventType downStreamEventType = null;
        switch (eventType) {
            case SKIP:
                downStreamEventType = RESET;
                break;
            case FAILED:
            case ABORT:
            case CHECK_FAILED:
            case CHECK_SUCCESS:
            case UPSTREAM_FAILED:
                //from not term state to term state
                if (!TaskRunPhase.isTermState(prePhase)) {
                    downStreamEventType = CONDITION_CHANGE;
                }
                break;
            case HANGUP:
                //from not term state to term state
                if (!TaskRunPhase.isTermState(prePhase) && !TaskRunPhase.isBlocked(prePhase)) {
                    downStreamEventType = CONDITION_CHANGE;
                }
                break;
            case SUBMIT:
            case WAIT:
            case RESCHEDULE:
                //from term state to not term state
                if (TaskRunPhase.isTermState(prePhase)) {
                    downStreamEventType = RESET;
                }
                break;
            default:
                downStreamEventType = null;
        }
        return downStreamEventType;
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

    private void updateDatabase(TaskRunStateMachine stateMachine, Integer nextPhase) {
        Long taskAttemptId = stateMachine.getTaskAttemptId();

        taskRunDao.updateTaskAttemptPhase(taskAttemptId, nextPhase, getQueuedAt(), getStartAt(),
                getEndAt(), getTermAt());
        if (!TaskRunPhase.isTermState(nextPhase)) {
            taskRunDao.resetTaskRunTimestampToNull(Arrays.asList(taskRunId), "term_at");
        }

        if (TaskRunPhase.isFailure(nextPhase)) {
            recordUpstreamFailed(taskRunId);
        }
    }

    private void recordUpstreamFailed(Long taskRunId) {
        //fetch all downstream task runs
        List<Long> downstreamTaskRunIds = taskRunDao.fetchDownStreamTaskRunIdsRecursive(taskRunId);
        logger.debug("fetch downStream taskRunIds = {},taskRunId = {}", downstreamTaskRunIds, taskRunId);

        //update downstream taskrun "failed_upstream_task_run_id" field with taskRunId by TaskRunPhase
        taskRunDao.updateTaskRunWithFailedUpstream(taskRunId, downstreamTaskRunIds, TaskRunPhase.UPSTREAM_FAILED);
    }


}
