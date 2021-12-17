package com.miotech.kun.workflow.common.taskrun.state;

import com.miotech.kun.workflow.core.model.taskrun.BasicTaskRunState;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunStatus;

public class TaskRunRunning extends BasicTaskRunState {

    public TaskRunRunning() {
        super(TaskRunStatus.RUNNING);
    }

    @Override
    protected TaskRunStatus onSubmit(){
        return TaskRunStatus.QUEUED;
    }

    @Override
    protected TaskRunStatus onAbort(){
        return TaskRunStatus.ABORTED;
    }

    @Override
    protected TaskRunStatus onFailed(){
        return TaskRunStatus.FAILED;
    }

    @Override
    protected TaskRunStatus onCheck(){
        return TaskRunStatus.CHECK;
    }

}
