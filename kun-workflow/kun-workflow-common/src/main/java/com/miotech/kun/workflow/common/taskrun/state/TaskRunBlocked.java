package com.miotech.kun.workflow.common.taskrun.state;

import com.miotech.kun.workflow.core.model.taskrun.BasicTaskRunState;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunStatus;

public class TaskRunBlocked extends BasicTaskRunState {

    public TaskRunBlocked() {
        super(TaskRunStatus.BLOCKED);
    }

    @Override
    protected TaskRunStatus onAbort(){
        return TaskRunStatus.ABORTED;
    }

    @Override
    protected TaskRunStatus onAwake(){
        return TaskRunStatus.CREATED;
    }

    @Override
    protected TaskRunStatus onUpstreamFailed(){
        return TaskRunStatus.UPSTREAM_FAILED;
    }

    @Override
    protected TaskRunStatus onSkip() {
        return TaskRunStatus.SKIPPED;
    }

}
