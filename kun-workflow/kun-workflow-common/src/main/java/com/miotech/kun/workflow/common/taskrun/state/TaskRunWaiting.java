package com.miotech.kun.workflow.common.taskrun.state;

import com.miotech.kun.workflow.core.model.taskrun.BasicTaskRunState;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunPhase;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunState;

/**
 * waiting upstream finished
 */
public class TaskRunWaiting extends BasicTaskRunState {


    public TaskRunWaiting(Integer taskRunParse) {
        super(taskRunParse);
    }

    @Override
    protected TaskRunState onSubmit() {
        return new TaskRunQueued(TaskRunPhase.QUEUED);
    }

    @Override
    protected TaskRunState onWait() {
        return this;
    }

    @Override
    protected TaskRunState onUpstreamFinished() {
        return new TaskRunWaiting(TaskRunPhase.WAITING);
    }

    @Override
    protected TaskRunState onHangup() {
        return new TaskRunBlocked(TaskRunPhase.BLOCKED);
    }

    @Override
    protected TaskRunState onUpstreamFailed() {
        return new TaskRunUpstreamFailed(TaskRunPhase.UPSTREAM_FAILED);
    }

    @Override
    protected TaskRunState onAssembled() {
        return this;
    }

    @Override
    protected TaskRunState onSkip() {
        return new TaskRunSkip(TaskRunPhase.SKIP);
    }

    @Override
    protected TaskRunState onReSet() {
        return this;
    }

    @Override
    protected TaskRunState onAbort() {
        return new TaskRunAborted(TaskRunPhase.ABORTED);
    }

    @Override
    protected TaskRunState onConditionRemoved() {
        return this;
    }

    @Override
    protected TaskRunState onRecover() {
        return this;
    }
}
