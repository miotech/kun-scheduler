package com.miotech.kun.workflow.scheduler;

import com.google.common.eventbus.EventBus;
import com.miotech.kun.commons.utils.Props;
import com.miotech.kun.workflow.TaskRunStateMachine;
import com.miotech.kun.workflow.TaskRunStateMachineDispatcher;
import com.miotech.kun.workflow.common.taskrun.bo.TaskAttemptProps;
import com.miotech.kun.workflow.common.taskrun.dao.TaskRunDao;
import com.miotech.kun.workflow.core.Executor;
import com.miotech.kun.workflow.core.event.FromTaskRunContext;
import com.miotech.kun.workflow.core.event.TaskRunTransitionEvent;
import com.miotech.kun.workflow.core.event.TaskRunTransitionEventType;
import com.miotech.kun.workflow.core.model.taskrun.*;
import com.miotech.kun.workflow.scheduler.action.*;
import com.miotech.kun.workflow.utils.WorkflowIdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;
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

    private final TaskRunStateMachineDispatcher stateMachineDispatcher;

    private final Map<Long, Boolean> rerunningTaskRunIds = new ConcurrentHashMap<>();


    @Inject
    public TaskManager(Executor executor, TaskRunDao taskRunDao, EventBus eventBus, Props props,
                       TaskRunStateMachineDispatcher stateMachineDispatcher) {
        this.executor = executor;
        this.taskRunDao = taskRunDao;
        this.props = props;
        this.stateMachineDispatcher = stateMachineDispatcher;

        this.eventBus = eventBus;
        init();

    }

    /* ----------- public methods ------------ */

    public void submit(List<TaskRun> taskRuns) {
        // 生成对应的TaskAttempt
        List<TaskAttempt> taskAttempts = taskRuns.stream()
                .map(taskRun -> createTaskAttempt(taskRun, true)).collect(Collectors.toList());
        logger.debug("TaskAttempts saved. total={}", taskAttempts.size());
        save(taskAttempts);
        for (TaskAttempt taskAttempt : taskAttempts) {
            try {
                TaskRunTransitionEvent transitionEvent = new TaskRunTransitionEvent(TaskRunTransitionEventType.ASSEMBLED, taskAttempt.getId(), null);
                eventBus.post(transitionEvent);
            } catch (Throwable e) {
                logger.error("send ASSEMBLED event after attempt {} created failed", taskAttempt.getId(), e);
            }

        }
    }

    /**
     * taskRun status must be success, failed, check_failed
     *
     * @param taskRun
     */
    public boolean retry(TaskRun taskRun) {
        checkState(taskRun.getStatus().allowRetry(), "taskRun status must be allowed to retry");
        // Does the same re-run request invoked in another threads?
        if (rerunningTaskRunIds.put(taskRun.getId(), Boolean.TRUE) != null) {
            logger.warn("Cannot rerun taskrun instance with id = {}. Reason: another thread is attempting to re-run the same task run.", taskRun.getId());
            return false;
        }
        TaskAttemptProps taskAttemptProps = taskRunDao.fetchLatestTaskAttempt(taskRun.getId());
        TaskRunTransitionEvent taskRunTransitionEvent = new TaskRunTransitionEvent(TaskRunTransitionEventType.RESCHEDULE, taskAttemptProps.getId(), null);
        eventBus.post(taskRunTransitionEvent);
        return true;
    }

    public boolean skip(TaskRun taskRun) {
        //0.check state of taskrun
        checkState(taskRun.getStatus().isFailure(), "This taskRun is not allowed to skip. Status not match");
        //1. reuse latest task attempt
        TaskAttemptProps taskAttempt = taskRunDao.fetchLatestTaskAttempt(taskRun.getId());

        //2. post skip taskrun transition event
        TaskRunTransitionEvent taskRunTransitionEvent = new TaskRunTransitionEvent(TaskRunTransitionEventType.SKIP, taskAttempt.getId(), null);
        eventBus.post(taskRunTransitionEvent);

        return true;
    }

    public boolean batchRemoveDependency(Long taskRunId, List<Long> upstreamTaskRunIdsToRemoveDependency) {
        TaskRun taskRun = taskRunDao.fetchTaskRunById(taskRunId).orElse(null);
        if (taskRun == null) {
            return false;
        }
        checkState(taskRun.getStatus().allowRemoveDependency(), "This taskrun is not allowed to remove dependency. Status not match");
        TaskAttemptProps taskAttempt = taskRunDao.fetchLatestTaskAttempt(taskRun.getId());
        List<TaskRun> upstreamTaskRunsToRemoveDependency = taskRunDao.fetchTaskRunsByIds(upstreamTaskRunIdsToRemoveDependency).stream()
                .map(r -> r.orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        //remove dependency in database
        taskRunDao.removeTaskRunDependency(taskRunId, upstreamTaskRunIdsToRemoveDependency);

        if (taskAttempt.getStatus().isUpstreamFailed()) {
            //fetch remained failed upstream task run ids
            List<TaskRun> remainedUpstreamTaskRuns = taskRunDao.fetchUpstreamTaskRunsById(taskRunId, 1, false);
            List<Long> remainedFailedUpstreamTaskRunIds = fetchFailedUpstreamTaskRunIdCollectionFromTaskRuns(remainedUpstreamTaskRuns);

            //fetch failed upstream taskrun ids to be removed
            List<Long> failedUpstreamTaskRunIdsToBeRemoved = fetchFailedUpstreamTaskRunIdCollectionFromTaskRuns(upstreamTaskRunsToRemoveDependency);

            //get intersects
            failedUpstreamTaskRunIdsToBeRemoved.removeAll(remainedFailedUpstreamTaskRunIds);

            // no need to update downstream, nothing changed
            if (failedUpstreamTaskRunIdsToBeRemoved.isEmpty()) {
                return true;
            }

            //update failed upstream task run ids
            List<Long> taskRunShouldUpdateFailedUpstreamTaskRunIds = new ArrayList<>();
            taskRunShouldUpdateFailedUpstreamTaskRunIds.addAll(taskRunDao.fetchDownStreamTaskRunIdsRecursive(taskRunId));
            taskRunShouldUpdateFailedUpstreamTaskRunIds.add(taskRunId);
            logger.debug("remove upstream failed taskRunIds {} for {}", failedUpstreamTaskRunIdsToBeRemoved, taskRunShouldUpdateFailedUpstreamTaskRunIds);
            taskRunDao.removeFailedUpstreamTaskRunIds(taskRunShouldUpdateFailedUpstreamTaskRunIds, failedUpstreamTaskRunIdsToBeRemoved);
        }

        for (TaskRun upstreamTaskRun : upstreamTaskRunsToRemoveDependency) {
            eventBus.post(new TaskRunTransitionEvent(TaskRunTransitionEventType.CONDITION_REMOVE, taskAttempt.getId(), new FromTaskRunContext(upstreamTaskRun.getId(), null, upstreamTaskRun.getTaskRunPhase(), upstreamTaskRun.getId())));
        }

        //trigger runnable taskRun
        return true;
    }

    public void removeDependency(TaskAttempt attempt, FromTaskRunContext fromTaskRunContext) {
        Long taskRunId = attempt.getTaskRun().getId();
        TaskRunStateMachine stateMachine = stateMachineDispatcher.fetchStateMachine(taskRunId);
        stateMachine.removeCondition(fromTaskRunContext.getTaskRunId(), fromTaskRunContext.getNextPhase());

        //reschedule taskRun if necessary
        eventBus.post(new TaskRunTransitionEvent(TaskRunTransitionEventType.ASSEMBLED, attempt.getId(), fromTaskRunContext));
    }

    public void resolveCondition(TaskAttempt taskAttempt, FromTaskRunContext fromTaskRunContext) {
        logger.debug("resolve condition for taskAttempt {}", taskAttempt.getId());
        TaskRunStateMachine stateMachine = stateMachineDispatcher.fetchStateMachine(taskAttempt.getTaskRun().getId());
        ConditionStatus conditionStatus = stateMachine.resolveCondition();
        TaskRunTransitionEventType nextEventType = null;
        switch (conditionStatus) {
            case WAITING:
                nextEventType = TaskRunTransitionEventType.WAIT;
                break;
            case SATISFY:
                nextEventType = TaskRunTransitionEventType.SUBMIT;
                break;
            case UPSTREAM_FAILED:
                nextEventType = TaskRunTransitionEventType.UPSTREAM_FAILED;
                break;
            case BLOCK:
                nextEventType = TaskRunTransitionEventType.HANGUP;
                break;
            default:
                nextEventType = null;
        }
        if (nextEventType != null) {
            eventBus.post(new TaskRunTransitionEvent(nextEventType, taskAttempt.getId(), fromTaskRunContext));
        }
    }

    public void conditionChange(TaskAttempt taskAttempt, FromTaskRunContext context) {
        Long taskRunId = taskAttempt.getTaskRun().getId();
        TaskRunStateMachine stateMachine = stateMachineDispatcher.fetchStateMachine(taskRunId);
        stateMachine.conditionChange(context.getTaskRunId(), context.getPrePhase(), context.getNextPhase());
        eventBus.post(new TaskRunTransitionEvent(TaskRunTransitionEventType.ASSEMBLED, taskAttempt.getId(), context));
    }

    public void reset(TaskAttempt taskAttempt, FromTaskRunContext context) {
        resetTermAt(taskAttempt.getTaskRun());
        logger.debug("reset upstream failed from {} for {}", context, taskAttempt.getTaskRun().getId());
        resetUpstreamFailed(context.getSrcTaskRunId(), taskAttempt.getTaskRun().getId());
        TaskRunStateMachine stateMachine = stateMachineDispatcher.fetchStateMachine(taskAttempt.getTaskRun().getId());
        stateMachine.conditionChange(context.getTaskRunId(), context.getPrePhase(), context.getNextPhase());
        eventBus.post(new TaskRunTransitionEvent(TaskRunTransitionEventType.ASSEMBLED, taskAttempt.getId(), context));

    }

    public void reschedule(TaskRun taskRun) {
        try {
            resetTaskRun(taskRun);
            //0. prepare task attempt
            TaskAttempt taskAttempt = createTaskAttempt(taskRun, false);
            logger.info("save rerun taskAttempt, taskAttemptId = {}, attempt = {}", taskAttempt.getId(), taskAttempt.getAttempt());
            save(Arrays.asList(taskAttempt));

            TaskRunTransitionEvent assembleEvent = new TaskRunTransitionEvent(TaskRunTransitionEventType.ASSEMBLED, taskAttempt.getId(), null);
            eventBus.post(assembleEvent);
        } catch (Exception e) {
            logger.error("Failed to re-run taskrun with id = {} due to exceptions.", taskRun.getId());
            throw e;
        } finally {
            // release the lock
            rerunningTaskRunIds.remove(taskRun.getId());
        }
    }

    public boolean abort(Long taskRunId) {
        TaskAttemptProps attempt = taskRunDao.fetchLatestTaskAttempt(taskRunId);

        if (Objects.isNull(attempt)) {
            throw new IllegalArgumentException("Attempt is not found for taskRunId: " + taskRunId);
        }
        TaskRunTransitionEvent taskRunTransitionEvent = new TaskRunTransitionEvent(TaskRunTransitionEventType.ABORT, attempt.getId(), null);
        eventBus.post(taskRunTransitionEvent);
        return true;
    }

    /* ----------- private methods ------------ */

    private void init() {
        stateMachineDispatcher.register(TaskRunPhase.CREATED, TaskRunTransitionEventType.ASSEMBLED, new TaskRunAssembledAction(this));
        stateMachineDispatcher.register(TaskRunPhase.UPSTREAM_FAILED, TaskRunTransitionEventType.RESET, new ResetAction(this));
        stateMachineDispatcher.register(TaskRunPhase.BLOCKED, TaskRunTransitionEventType.RESET, new ResetAction(this));
        stateMachineDispatcher.register(TaskRunPhase.WAITING, TaskRunTransitionEventType.RESET, new ResetAction(this));
        stateMachineDispatcher.register(TaskRunPhase.CREATED, TaskRunTransitionEventType.CONDITION_CHANGE, new TaskRunUpstreamChangeAction(this));
        stateMachineDispatcher.register(TaskRunPhase.UPSTREAM_FAILED, TaskRunTransitionEventType.CONDITION_CHANGE, new TaskRunUpstreamChangeAction(this));
        stateMachineDispatcher.register(TaskRunPhase.BLOCKED, TaskRunTransitionEventType.CONDITION_CHANGE, new TaskRunUpstreamChangeAction(this));
        stateMachineDispatcher.register(TaskRunPhase.WAITING, TaskRunTransitionEventType.CONDITION_CHANGE, new TaskRunUpstreamChangeAction(this));
        stateMachineDispatcher.register(TaskRunPhase.UPSTREAM_FAILED, TaskRunTransitionEventType.ASSEMBLED, new TaskRunAssembledAction(this));
        stateMachineDispatcher.register(TaskRunPhase.BLOCKED, TaskRunTransitionEventType.ASSEMBLED, new TaskRunAssembledAction(this));
        stateMachineDispatcher.register(TaskRunPhase.WAITING, TaskRunTransitionEventType.ASSEMBLED, new TaskRunAssembledAction(this));
        stateMachineDispatcher.register(TaskRunPhase.UPSTREAM_FAILED, TaskRunTransitionEventType.SUBMIT, new TaskRunSubmitAction(executor));
        stateMachineDispatcher.register(TaskRunPhase.BLOCKED, TaskRunTransitionEventType.SUBMIT, new TaskRunSubmitAction(executor));
        stateMachineDispatcher.register(TaskRunPhase.WAITING, TaskRunTransitionEventType.SUBMIT, new TaskRunSubmitAction(executor));
        stateMachineDispatcher.register(TaskRunPhase.RUNNING, TaskRunTransitionEventType.RESUBMIT, new TaskRunSubmitAction(executor));
        stateMachineDispatcher.register(TaskRunPhase.FAILED, TaskRunTransitionEventType.RESCHEDULE, new TaskRunRetryAction(this));
        stateMachineDispatcher.register(TaskRunPhase.CHECK_FAILED, TaskRunTransitionEventType.RESCHEDULE, new TaskRunRetryAction(this));
        stateMachineDispatcher.register(TaskRunPhase.ABORTED, TaskRunTransitionEventType.RESCHEDULE, new TaskRunRetryAction(this));
        stateMachineDispatcher.register(TaskRunPhase.SUCCESS, TaskRunTransitionEventType.RESCHEDULE, new TaskRunRetryAction(this));
        stateMachineDispatcher.register(TaskRunPhase.UPSTREAM_FAILED, TaskRunTransitionEventType.CONDITION_REMOVE, new RemoveDependencyAction(this));
        stateMachineDispatcher.register(TaskRunPhase.BLOCKED, TaskRunTransitionEventType.CONDITION_REMOVE, new RemoveDependencyAction(this));
        stateMachineDispatcher.register(TaskRunPhase.WAITING, TaskRunTransitionEventType.CONDITION_REMOVE, new RemoveDependencyAction(this));
        stateMachineDispatcher.register(TaskRunPhase.QUEUED, TaskRunTransitionEventType.READY, new TaskRunStartAction(executor));
        stateMachineDispatcher.register(TaskRunPhase.RUNNING, TaskRunTransitionEventType.COMPLETE, new TaskRunCheckAction(executor));
        stateMachineDispatcher.register(TaskRunPhase.CREATED, TaskRunTransitionEventType.RECOVER, new TaskRunAssembledAction(this));
        stateMachineDispatcher.register(TaskRunPhase.WAITING, TaskRunTransitionEventType.RECOVER, new TaskRunAssembledAction(this));
        stateMachineDispatcher.register(TaskRunPhase.QUEUED, TaskRunTransitionEventType.RECOVER, new TaskRunSubmitAction(executor));
        stateMachineDispatcher.register(TaskRunPhase.RUNNING, TaskRunTransitionEventType.RECOVER, new TaskRunStartAction(executor));
        stateMachineDispatcher.register(TaskRunPhase.QUEUED, TaskRunTransitionEventType.ABORT, new TaskRunAbortAction(executor));
        stateMachineDispatcher.register(TaskRunPhase.RUNNING, TaskRunTransitionEventType.ABORT, new TaskRunAbortAction(executor));

    }

    private TaskAttempt createTaskAttempt(TaskRun taskRun, boolean reUseLatest) {
        checkNotNull(taskRun, "taskRun should not be null.");
        checkNotNull(taskRun.getId(), "taskRun's id should not be null.");

        TaskAttemptProps savedTaskAttempt = taskRunDao.fetchLatestTaskAttempt(taskRun.getId());

        int attempt = 1;
        if (savedTaskAttempt != null) {
            attempt = savedTaskAttempt.getAttempt() + 1;
        } else {
            reUseLatest = false;
        }
        TaskAttempt taskAttempt = TaskAttempt.newBuilder()
                .withId(reUseLatest ? savedTaskAttempt.getId() : WorkflowIdGenerator.nextTaskAttemptId(taskRun.getId(), attempt))
                .withTaskRun(taskRun)
                .withAttempt(reUseLatest ? savedTaskAttempt.getAttempt() : attempt)
                .withStatus(reUseLatest ? savedTaskAttempt.getStatus() : determineInitStatus(taskRun))
                .withQueueName(taskRun.getQueueName())
                .withPriority(taskRun.getPriority())
                .withRetryTimes(0)
                .withExecutorLabel(taskRun.getExecutorLabel())
                .withPhase(TaskRunPhase.CREATED)
                .build();
        logger.debug("Created taskAttempt. taskAttemptId = {}", taskAttempt.getId());

        return taskAttempt;
    }

    private TaskRunStatus determineInitStatus(TaskRun taskRun) {
        return detectUpstreamFailedOrBlocked(taskRun) ? taskRun.getStatus() : TaskRunStatus.CREATED;
    }

    private boolean detectUpstreamFailedOrBlocked(TaskRun taskRun) {
        return taskRun.getStatus().equals(TaskRunStatus.UPSTREAM_FAILED) ||
                taskRun.getStatus().equals(TaskRunStatus.BLOCKED);
    }

    private void save(List<TaskAttempt> taskAttempts) {
        for (TaskAttempt ta : taskAttempts) {
            taskRunDao.createAttempt(ta);
        }
    }

    private List<Long> fetchFailedUpstreamTaskRunIdCollectionFromTaskRuns(List<TaskRun> upstreamTaskRuns) {
        List<Long> failedUpstreamTaskRunIds = new ArrayList<>();
        for (TaskRun upstreamTaskRun : upstreamTaskRuns) {
            if (upstreamTaskRun.getStatus().isFailure()) {
                failedUpstreamTaskRunIds.add(upstreamTaskRun.getId());
            } else if (upstreamTaskRun.getStatus().isUpstreamFailed()) {
                failedUpstreamTaskRunIds.addAll(upstreamTaskRun.getFailedUpstreamTaskRunIds());
            }
        }
        return failedUpstreamTaskRunIds;
    }

    private void resetTaskRun(TaskRun taskRun) {
        resetTermAt(taskRun);
    }


    private void resetTermAt(TaskRun taskRun) {
        taskRunDao.resetTaskRunTimestampToNull(Arrays.asList(taskRun.getId()), "term_at");
    }

    private void resetUpstreamFailed(Long upstreamTaskRunId, Long downstreamTaskRunId) {
        List<Long> downstreamTaskRunIds = Arrays.asList(downstreamTaskRunId);

        //update downstream taskrun "failed_upstream_task_run_id" field with taskRunId by TaskRunPhase
        taskRunDao.updateTaskRunWithFailedUpstream(upstreamTaskRunId, downstreamTaskRunIds, TaskRunPhase.CREATED);
    }


}
