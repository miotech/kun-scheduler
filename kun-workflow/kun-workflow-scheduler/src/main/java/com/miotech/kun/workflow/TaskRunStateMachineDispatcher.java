package com.miotech.kun.workflow;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Singleton;
import com.miotech.kun.workflow.common.lineage.service.LineageService;
import com.miotech.kun.workflow.common.taskrun.dao.TaskRunDao;
import com.miotech.kun.workflow.common.taskrun.state.*;
import com.miotech.kun.workflow.core.annotation.Internal;
import com.miotech.kun.workflow.core.event.TaskRunTransitionEvent;
import com.miotech.kun.workflow.core.event.TaskRunTransitionEventType;
import com.miotech.kun.workflow.core.model.taskrun.TaskAttempt;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunAction;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunPhase;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * manager taskRun status transition
 */
@Singleton
public class TaskRunStateMachineDispatcher {

    private final Logger logger = LoggerFactory.getLogger(TaskRunStateMachineDispatcher.class);
    private final Map<Integer, TaskRunState> taskRunStateMap = new HashMap<>();

    private final TaskRunDao taskRunDao;
    private final EventBus eventBus;
    private final LineageService lineageService;
    private final TaskRunStateMachineBuffer buffer;
    private final TaskRunSMExecutor taskRunSMExecutor;

    public void register(Integer taskRunPhase, TaskRunTransitionEventType eventType, TaskRunAction action) {
        TaskRunState taskRunState = taskRunStateMap.get(taskRunPhase);
        taskRunState.registerAction(eventType, action);
        logger.info("register action for phase : {} , receive event : {}", taskRunPhase, eventType.name());
    }


    @Inject
    public TaskRunStateMachineDispatcher(TaskRunDao taskRunDao, EventBus eventBus, LineageService lineageService,
                                         TaskRunSMExecutor taskRunSMExecutor, TaskRunStateMachineBuffer buffer) {
        this.taskRunDao = taskRunDao;
        this.eventBus = eventBus;
        this.lineageService = lineageService;
        this.taskRunSMExecutor = taskRunSMExecutor;
        this.buffer = buffer;
        taskRunStateMap.put(TaskRunPhase.CREATED, new TaskRunCreated(TaskRunPhase.CREATED));
        taskRunStateMap.put(TaskRunPhase.WAITING, new TaskRunWaiting(TaskRunPhase.WAITING));
        taskRunStateMap.put(TaskRunPhase.QUEUED, new TaskRunQueued(TaskRunPhase.QUEUED));
        taskRunStateMap.put(TaskRunPhase.RUNNING, new TaskRunRunning(TaskRunPhase.RUNNING));
        taskRunStateMap.put(TaskRunPhase.CHECKING, new TaskRunCheck(TaskRunPhase.CHECKING));
        taskRunStateMap.put(TaskRunPhase.CHECK_FAILED, new TaskRunCheckFailed(TaskRunPhase.CHECK_FAILED));
        taskRunStateMap.put(TaskRunPhase.FAILED, new TaskRunFailed(TaskRunPhase.FAILED));
        taskRunStateMap.put(TaskRunPhase.ABORTED, new TaskRunAborted(TaskRunPhase.ABORTED));
        taskRunStateMap.put(TaskRunPhase.ERROR, new TaskRunError(TaskRunPhase.ERROR));
        taskRunStateMap.put(TaskRunPhase.SUCCESS, new TaskRunSuccess(TaskRunPhase.SUCCESS));
        taskRunStateMap.put(TaskRunPhase.UPSTREAM_FAILED, new TaskRunUpstreamFailed(TaskRunPhase.UPSTREAM_FAILED));
        taskRunStateMap.put(TaskRunPhase.BLOCKED, new TaskRunBlocked(TaskRunPhase.BLOCKED));
    }

    public void recover() {
        logger.debug("clean expired events...");
        taskRunDao.cleanExpiredEvents();
        logger.debug("recover state machine...");
        List<TaskAttempt> notTermAttempts = taskRunDao.shouldRecoverAttempts();
        logger.debug("going to recover {} stateMachine", notTermAttempts.size());
        for (TaskAttempt notTermAttempt : notTermAttempts) {
            TaskRunStateMachine stateMachine = generateStateMache(notTermAttempt);
            stateMachine.recover();
            buffer.put(stateMachine);
        }
        start();
        for (TaskRunStateMachine stateMachine : buffer.getAll()) {
            hintToExecute(stateMachine);
        }
    }

    public void start() {
        logger.debug("start state machine dispatcher...");
        eventBus.register(this);
    }

    /**
     * just for test
     */
    @Internal
    public void reset() {
        buffer.reset();
        eventBus.unregister(this);
    }

    public void handleEvent(TaskRunTransitionEvent taskRunTransitionEvent) {
        logger.debug("handle event : {}", taskRunTransitionEvent);
        Long taskAttemptId = taskRunTransitionEvent.getTaskAttemptId();
        Optional<TaskAttempt> taskAttemptOption = taskRunDao.fetchAttemptById(taskAttemptId);
        if (!taskAttemptOption.isPresent()) {
            logger.warn("taskAttempt = {}, is not exist", taskAttemptId);
            return;
        }

        preTransition(taskRunTransitionEvent);
        TaskAttempt taskAttempt = taskAttemptOption.get();
        TaskRunStateMachine stateMachine = generateStateMache(taskAttempt);
        stateMachine.receiveEvent(taskRunTransitionEvent);
        hintToExecute(stateMachine);
    }

    public TaskRunStateMachine fetchStateMachine(Long taskRunId) {
        return buffer.get(taskRunId);
    }


    private void preTransition(TaskRunTransitionEvent event) {
        //save event to database if not exist
        taskRunDao.saveTransitEvent(event, false);
    }

    private TaskRunStateMachine generateStateMache(TaskAttempt taskAttempt) {
        Long taskRunId = taskAttempt.getTaskRun().getId();
        TaskRunStateMachine stateMachine = fetchStateMachine(taskRunId);
        if (stateMachine != null && !stateMachine.isClosed()) {
            return stateMachine;
        }
        logger.debug("stateMachine for attempt : {} is not exists,create new one", taskAttempt.getId());
        stateMachine = createStateMachine(taskAttempt);

        return stateMachine;
    }

    private void hintToExecute(TaskRunStateMachine stateMachine) {
        logger.debug("hint stateMachine {} to execute", stateMachine.getTaskRunId());
        taskRunSMExecutor.registerForExec(stateMachine);
    }

    private synchronized TaskRunStateMachine createStateMachine(TaskAttempt taskAttempt) {
        Long taskRunId = taskAttempt.getTaskRun().getId();
        TaskRunStateMachine stateMachine = fetchStateMachine(taskRunId);
        //recheck
        if (stateMachine != null && !stateMachine.isClosed()) {
            return stateMachine;
        }
        TaskRunStateMachine newStateMachine = new TaskRunStateMachine(taskAttempt, taskRunStateMap, eventBus, lineageService, taskRunDao);
        newStateMachine.generateCondition();
        buffer.put(newStateMachine);
        return newStateMachine;
    }

    @Subscribe
    public void onReceive(TaskRunTransitionEvent event) {
        handleEvent(event);
    }

}
