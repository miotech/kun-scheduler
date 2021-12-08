package com.miotech.kun.workflow.common.taskrun.state;

import com.miotech.kun.workflow.core.model.taskrun.BasicTaskRunState;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunStatus;

public class TaskRunQueued extends BasicTaskRunState {

    public TaskRunQueued() {
        super(TaskRunStatus.QUEUED);
    }

    @Override
    protected TaskRunStatus onAbort(){
        return TaskRunStatus.ABORTED;
    }

    @Override
    protected TaskRunStatus onRunning(){
        return TaskRunStatus.RUNNING;
    }

    @Override
    protected TaskRunStatus onException(){
        return TaskRunStatus.ERROR;
    }
}
