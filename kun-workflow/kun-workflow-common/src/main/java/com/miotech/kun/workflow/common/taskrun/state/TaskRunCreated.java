package com.miotech.kun.workflow.common.taskrun.state;

import com.miotech.kun.workflow.core.model.taskrun.BasicTaskRunState;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunStatus;

public class TaskRunCreated extends BasicTaskRunState {

    public TaskRunCreated() {
        super(TaskRunStatus.CREATED);
    }

    @Override
    protected TaskRunStatus onAbort(){
        return TaskRunStatus.ABORTED;
    }

    @Override
    protected TaskRunStatus onSubmit(){
        return TaskRunStatus.QUEUED;
    }

    @Override
    protected TaskRunStatus onUpstreamFailed(){
        return TaskRunStatus.UPSTREAM_FAILED;
    }

    @Override
    protected TaskRunStatus onHangup(){
        return TaskRunStatus.BLOCKED;
    }

}
