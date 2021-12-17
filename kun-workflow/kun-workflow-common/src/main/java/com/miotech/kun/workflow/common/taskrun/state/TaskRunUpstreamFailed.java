package com.miotech.kun.workflow.common.taskrun.state;

import com.miotech.kun.workflow.core.model.taskrun.BasicTaskRunState;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunStatus;

public class TaskRunUpstreamFailed extends BasicTaskRunState {

    public TaskRunUpstreamFailed() {
        super(TaskRunStatus.UPSTREAM_FAILED);
    }

    protected TaskRunStatus onReschedule(){
        return TaskRunStatus.CREATED;
    }

}
