package com.miotech.kun.workflow.common.taskrun.state;

import com.miotech.kun.workflow.core.model.taskrun.BasicTaskRunState;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunPhase;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunState;

public class TaskRunUpstreamFailed extends BasicTaskRunState {


    public TaskRunUpstreamFailed(Integer taskRunParse) {
        super(taskRunParse);
    }

    @Override
    protected TaskRunState onReSet() {
        return new TaskRunUpstreamFailed(TaskRunPhase.UPSTREAM_FAILED);
    }

    @Override
    protected TaskRunState onWait() {
        return new TaskRunWaiting(TaskRunPhase.WAITING);
    }

    @Override
    protected TaskRunState onHangup() {
        return new TaskRunBlocked(TaskRunPhase.BLOCKED);
    }

    @Override
    protected TaskRunState onUpstreamFailed() {
        return this;
    }

    @Override
    protected TaskRunState onSkip() {
        return new TaskRunSkip(TaskRunPhase.SKIP);
    }

    @Override
    protected TaskRunState onUpstreamFinished() {
        return this;
    }

    @Override
    protected TaskRunState onAssembled() {
        return this;
    }

    @Override
    protected TaskRunState onSubmit() {
        return new TaskRunQueued(TaskRunPhase.QUEUED);
    }

    @Override
    protected TaskRunState onConditionRemoved() {
        return this;
    }
}
