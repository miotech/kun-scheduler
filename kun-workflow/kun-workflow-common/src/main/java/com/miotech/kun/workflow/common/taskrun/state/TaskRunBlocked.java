package com.miotech.kun.workflow.common.taskrun.state;

import com.miotech.kun.workflow.core.model.taskrun.BasicTaskRunState;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunStatus;

public class TaskRunBlocked extends BasicTaskRunState {

    public TaskRunBlocked() {
        super(TaskRunStatus.BLOCKED);
    }

    protected TaskRunStatus onAbort(){
        return TaskRunStatus.ABORTED;
    }

    protected TaskRunStatus onAwake(){
        return TaskRunStatus.CREATED;
    }

    protected TaskRunStatus onUpstreamFailed(){
        return TaskRunStatus.UPSTREAM_FAILED;
    }

}
