package com.miotech.kun.workflow.common.taskrun.state;

import com.miotech.kun.workflow.core.model.taskrun.BasicTaskRunState;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunPhase;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunState;

public class TaskRunCheck extends BasicTaskRunState {


    public TaskRunCheck(Integer taskRunParse) {
        super(taskRunParse);
    }

    @Override
    protected TaskRunState onCheckSuccess() {
        return new TaskRunSuccess(TaskRunPhase.SUCCESS);
    }

    @Override
    protected TaskRunState onCheckFailed() {
        return new TaskRunCheckFailed(TaskRunPhase.CHECK_FAILED);
    }
}
