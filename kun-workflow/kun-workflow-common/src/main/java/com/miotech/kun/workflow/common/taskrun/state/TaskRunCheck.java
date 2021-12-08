package com.miotech.kun.workflow.common.taskrun.state;

import com.miotech.kun.workflow.core.model.taskrun.BasicTaskRunState;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunStatus;

public class TaskRunCheck extends BasicTaskRunState {

    public TaskRunCheck() {
        super(TaskRunStatus.CHECK);
    }

    @Override
    protected TaskRunStatus onAbort(){
        return TaskRunStatus.ABORTED;
    }

    @Override
    protected TaskRunStatus onCheckSuccess(){
        return TaskRunStatus.SUCCESS;
    }

    @Override
    protected TaskRunStatus onCheckFailed(){
        return TaskRunStatus.CHECK_FAILED;
    }

}
