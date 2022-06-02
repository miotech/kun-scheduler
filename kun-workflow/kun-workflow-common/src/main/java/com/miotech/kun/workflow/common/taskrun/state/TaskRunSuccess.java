package com.miotech.kun.workflow.common.taskrun.state;

import com.miotech.kun.workflow.core.model.taskrun.BasicTaskRunState;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunStatus;

public class TaskRunSuccess extends BasicTaskRunState {

    public TaskRunSuccess() {
        super(TaskRunStatus.SUCCESS);
    }

    @Override
    protected TaskRunStatus onReschedule(){
        return TaskRunStatus.CREATED;
    }
}
