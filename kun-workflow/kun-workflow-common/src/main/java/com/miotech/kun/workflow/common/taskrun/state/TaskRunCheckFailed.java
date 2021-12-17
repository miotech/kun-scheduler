package com.miotech.kun.workflow.common.taskrun.state;

import com.miotech.kun.workflow.core.model.taskrun.BasicTaskRunState;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunStatus;

public class TaskRunCheckFailed extends BasicTaskRunState {

    public TaskRunCheckFailed() {
        super(TaskRunStatus.CHECK_FAILED);
    }

    @Override
    protected TaskRunStatus onReschedule(){
        return TaskRunStatus.CREATED;
    }

}
