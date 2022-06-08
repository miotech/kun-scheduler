package com.miotech.kun.workflow.core.model.taskrun;

import com.miotech.kun.workflow.core.event.TaskRunTransitionEvent;
import com.miotech.kun.workflow.core.event.TaskRunTransitionEventType;

public class BasicTaskRunState implements TaskRunState{

    private final TaskRunStatus taskRunStatus;

    public BasicTaskRunState(TaskRunStatus taskRunStatus) {
        this.taskRunStatus = taskRunStatus;
    }

    @Override
    public TaskRunStatus doTransition(TaskRunTransitionEvent taskRunTransitionEvent) {
        TaskRunTransitionEventType eventType = taskRunTransitionEvent.getType();
        TaskRunStatus nextStatus = null;
        switch (eventType){
            case RESCHEDULE:
                nextStatus = onReschedule();
                break;
            case SUBMIT:
                nextStatus = onSubmit();
                break;
            case EXCEPTION:
                nextStatus = onException();
                break;
            case RUNNING:
                nextStatus = onRunning();
                break;
            case UPSTREAM_FAILED:
                nextStatus = onUpstreamFailed();
                break;
            case HANGUP:
                nextStatus = onHangup();
                break;
            case AWAKE:
                nextStatus = onAwake();
                break;
            case FAILED:
                nextStatus = onFailed();
                break;
            case CHECK:
                nextStatus = onCheck();
                break;
            case CHECK_SUCCESS:
                nextStatus = onCheckSuccess();
                break;
            case CHECK_FAILED:
                nextStatus = onCheckFailed();
                break;
            case ABORT:
                nextStatus = onAbort();
                break;
            case SKIP:
                nextStatus = onSkip();
                break;
            default:
                //do nothing
        }
        return nextStatus;
    }

    protected TaskRunStatus onReschedule(){
        throw new IllegalStateException("Reschedule is not support for status : " + taskRunStatus);
    }

    protected TaskRunStatus onSubmit(){
        throw new IllegalStateException("Submit is not support for status : " + taskRunStatus);
    }

    protected TaskRunStatus onException(){
        throw new IllegalStateException("exception is not support for status : " + taskRunStatus);
    }

    protected TaskRunStatus onRunning(){
        throw new IllegalStateException("Running is not support for status : " + taskRunStatus);
    }

    protected TaskRunStatus onCheck(){
        throw new IllegalStateException("Check is not support for status : " + taskRunStatus);
    }

    protected TaskRunStatus onFailed(){
        throw new IllegalStateException("Failed is not support for status : " + taskRunStatus);
    }

    protected TaskRunStatus onUpstreamFailed(){
        throw new IllegalStateException("UpstreamFailed is not support for status : " + taskRunStatus);
    }

    protected TaskRunStatus onHangup(){
        throw new IllegalStateException("Hangup is not support for status : " + taskRunStatus);
    }

    protected TaskRunStatus onAbort(){
        throw new IllegalStateException("Abort is not support for status : " + taskRunStatus);
    }

    protected TaskRunStatus onCheckSuccess(){
        throw new IllegalStateException("CheckSuccess is not support for status : " + taskRunStatus);
    }

    protected TaskRunStatus onCheckFailed(){
        throw new IllegalStateException("CheckFailed is not support for status : " + taskRunStatus);
    }

    protected TaskRunStatus onAwake(){
        throw new IllegalStateException("Awake is not support for status : " + taskRunStatus);
    }
    protected TaskRunStatus onSkip() {
        throw new IllegalStateException("Skip is not support for status: " + taskRunStatus);
    }

    @Override
    public TaskRunStatus getStatus() {
        return taskRunStatus;
    }
}
