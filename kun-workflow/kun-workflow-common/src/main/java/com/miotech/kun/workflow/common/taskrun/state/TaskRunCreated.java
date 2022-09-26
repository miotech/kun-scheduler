package com.miotech.kun.workflow.common.taskrun.state;

import com.miotech.kun.workflow.core.model.taskrun.BasicTaskRunState;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunPhase;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunState;

public class TaskRunCreated extends BasicTaskRunState {

    public TaskRunCreated(Integer taskRunParse) {
        super(taskRunParse);
    }

    @Override
    protected TaskRunState onAssembled() {
        //after transit to TaskRunAssembled,taskRun can be schedule
        return new TaskRunWaiting(TaskRunPhase.WAITING);
    }

    @Override
    protected TaskRunState onUpstreamFailed() {
        return new TaskRunUpstreamFailed(TaskRunPhase.UPSTREAM_FAILED);
    }

    @Override
    protected TaskRunState onAbort() {
        return new TaskRunAborted(TaskRunPhase.ABORTED);
    }

    @Override
    protected TaskRunState onUpstreamFinished() {
        return this;
    }

    @Override
    protected TaskRunState onRecover() {
        return new TaskRunWaiting(TaskRunPhase.WAITING);
    }
}
