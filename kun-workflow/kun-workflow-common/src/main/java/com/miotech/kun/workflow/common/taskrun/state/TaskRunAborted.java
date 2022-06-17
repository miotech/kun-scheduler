package com.miotech.kun.workflow.common.taskrun.state;

import com.miotech.kun.workflow.core.model.taskrun.BasicTaskRunState;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunStatus;

public class TaskRunAborted extends BasicTaskRunState {

    public TaskRunAborted(){
        super(TaskRunStatus.ABORTED);
    }

    @Override
    protected TaskRunStatus onReschedule() {
        return TaskRunStatus.CREATED;
    }

    @Override
    protected TaskRunStatus onSkip() {
        return TaskRunStatus.SKIPPED;
    }

}
