package com.miotech.kun.workflow.common.taskrun.state;

import com.miotech.kun.workflow.core.model.taskrun.BasicTaskRunState;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunStatus;

public class TaskRunError extends BasicTaskRunState {

    public TaskRunError() {
        super(TaskRunStatus.ERROR);
    }

    @Override
    protected TaskRunStatus onSubmit(){
        return TaskRunStatus.QUEUED;
    }
}
