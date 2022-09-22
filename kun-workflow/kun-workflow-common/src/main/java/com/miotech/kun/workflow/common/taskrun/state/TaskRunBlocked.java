package com.miotech.kun.workflow.common.taskrun.state;

import com.miotech.kun.workflow.core.model.taskrun.BasicTaskRunState;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunPhase;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunState;

public class TaskRunBlocked extends BasicTaskRunState {


    public TaskRunBlocked(Integer taskRunParse) {
        super(taskRunParse);
    }

    @Override
    protected TaskRunState onUpstreamFinished() {
        return new TaskRunBlocked(TaskRunPhase.BLOCKED);
    }

    @Override
    protected TaskRunState onUpstreamFailed() {
        return new TaskRunUpstreamFailed(TaskRunPhase.UPSTREAM_FAILED);
    }

    @Override
    protected TaskRunState onSubmit() {
        return new TaskRunQueued(TaskRunPhase.QUEUED);
    }

    @Override
    protected TaskRunState onAssembled() {
        return this;
    }

    @Override
    protected TaskRunState onHangup() {
        return this;
    }

    @Override
    protected TaskRunState onWait() {
        return new TaskRunWaiting(TaskRunPhase.WAITING);
    }

    @Override
    protected TaskRunState onReSet() {
        return this;
    }

    @Override
    protected TaskRunState onConditionRemoved() {
        return this;
    }
}
