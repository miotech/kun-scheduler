package com.miotech.kun.workflow.common.taskrun.state;

import com.miotech.kun.workflow.core.model.taskrun.BasicTaskRunState;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunPhase;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunState;

public class TaskRunError extends BasicTaskRunState {

    public TaskRunError(Integer taskRunParse) {
        super(taskRunParse);
    }

    @Override
    protected TaskRunState onSkip() {
        return new TaskRunSkip(TaskRunPhase.SKIP);
    }
}
