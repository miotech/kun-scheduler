package com.miotech.kun.workflow.core.model.taskrun;

import com.miotech.kun.workflow.core.event.TaskRunTransitionEvent;
import com.miotech.kun.workflow.core.event.TaskRunTransitionEventType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class BasicTaskRunState implements TaskRunState {

    protected final Logger logger = LoggerFactory.getLogger(BasicTaskRunState.class);
    protected Integer taskRunParse;
    private Map<TaskRunTransitionEventType, TaskRunAction> taskRunActionMap = new HashMap<>();

    public BasicTaskRunState(Integer taskRunParse) {
        this.taskRunParse = taskRunParse;
    }

    @Override
    public TaskRunState doTransition(TaskRunSMMessage message) {
        TaskRunTransitionEvent taskRunTransitionEvent = message.getEvent();
        TaskRunTransitionEventType eventType = taskRunTransitionEvent.getType();
        TaskRunState nextState = null;
        switch (eventType) {
            case RECOVER:
                nextState = onRecover();
                break;
            case RESCHEDULE:
                nextState = onReschedule();
                break;
            case RESET:
                nextState = onReSet();
                break;
            case ASSEMBLED:
                nextState = onAssembled();
                break;
            case WAIT:
                nextState = onWait();
                break;
            case SUBMIT:
                nextState = onSubmit();
                break;
            case EXCEPTION:
                nextState = onException();
                break;
            case READY:
                nextState = onReady();
                break;
            case UPSTREAM_FAILED:
                nextState = onUpstreamFailed();
                break;
            case HANGUP:
                nextState = onHangup();
                break;
            case FAILED:
                nextState = onFailed();
                break;
            case COMPLETE:
                nextState = onCheck();
                break;
            case CHECK_SUCCESS:
                nextState = onCheckSuccess();
                break;
            case CHECK_FAILED:
                nextState = onCheckFailed();
                break;
            case ABORT:
                nextState = onAbort();
                break;
            case SKIP:
                nextState = onSkip();
                break;
            case CONDITION_CHANGE:
                nextState = onUpstreamFinished();
                break;
            case CONDITION_REMOVE:
                nextState = onConditionRemoved();
                break;
            case RESUBMIT:
                nextState = onResubmit();
            default:
                //do nothing
        }
        TaskRunAction taskRunAction = taskRunActionMap.get(eventType);
        logger.info("going to do action for {} when receive event {}", taskRunParse, eventType.name());
        if (taskRunAction == null) {
            logger.warn("not action register for {} when receive event {}", taskRunParse, eventType.name());
        } else {
            taskRunAction.run(message);
        }

        return nextState;
    }

    @Override
    public void afterTransition(TaskRunTransitionEvent taskRunTransitionEvent, TaskAttempt taskAttempt) {
        //do nothing
    }

    @Override
    public void registerAction(TaskRunTransitionEventType eventType, TaskRunAction taskRunAction) {
        taskRunActionMap.put(eventType, taskRunAction);
    }

    @Override
    public Integer getPhase() {
        return taskRunParse;
    }

    @Override
    public String toString() {
        return "BasicTaskRunState{" +
                "taskRunParse=" + taskRunParse +
                '}';
    }

    protected TaskRunState onRecover(){
        throw new IllegalStateException("Recover is not support for phase : " + taskRunParse);
    }

    protected TaskRunState onReSet() {
        throw new IllegalStateException("Reset is not support for phase : " + taskRunParse);
    }

    protected TaskRunState onAssembled() {
        throw new IllegalStateException("Assembled is not support for phase : " + taskRunParse);
    }

    protected TaskRunState onWait(){
        throw new IllegalStateException("Wait is not support for phase : " + taskRunParse);
    }

    protected TaskRunState onUpstreamFinished() {
        throw new IllegalStateException("UpstreamFinished is not support for phase : " + taskRunParse);
    }

    protected TaskRunState onConditionRemoved(){
        throw new IllegalStateException("ConditionRemoved is not support for phase : " + taskRunParse);
    }

    protected TaskRunState onReady() {
        throw new IllegalStateException("ready is not support for phase : " + taskRunParse);
    }

    protected TaskRunState onReschedule() {
        throw new IllegalStateException("Reschedule is not support for phase : " + taskRunParse);
    }

    protected TaskRunState onSubmit() {
        throw new IllegalStateException("Submit is not support for phase : " + taskRunParse);
    }

    protected TaskRunState onException() {
        throw new IllegalStateException("exception is not support for phase : " + taskRunParse);
    }

    protected TaskRunState onCheck() {
        throw new IllegalStateException("Check is not support for phase : " + taskRunParse);
    }

    protected TaskRunState onFailed() {
        throw new IllegalStateException("Failed is not support for phase : " + taskRunParse);
    }

    protected TaskRunState onUpstreamFailed() {
        throw new IllegalStateException("UpstreamFailed is not support for phase : " + taskRunParse);
    }

    protected TaskRunState onHangup() {
        throw new IllegalStateException("Hangup is not support for phase : " + taskRunParse);
    }

    protected TaskRunState onAbort() {
        throw new IllegalStateException("Abort is not support for phase : " + taskRunParse);
    }

    protected TaskRunState onCheckSuccess() {
        throw new IllegalStateException("CheckSuccess is not support for phase : " + taskRunParse);
    }

    protected TaskRunState onCheckFailed() {
        throw new IllegalStateException("CheckFailed is not support for phase : " + taskRunParse);
    }

    protected TaskRunState onSkip() {
        throw new IllegalStateException("Skip is not support for phase: " + taskRunParse);
    }

    protected TaskRunState onResubmit() {
        throw new IllegalStateException("Resubmit is not support for phase: " + taskRunParse);
    }

}
