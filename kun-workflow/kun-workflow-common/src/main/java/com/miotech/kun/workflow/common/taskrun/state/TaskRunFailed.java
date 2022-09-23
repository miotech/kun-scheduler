package com.miotech.kun.workflow.common.taskrun.state;

import com.miotech.kun.workflow.core.model.taskrun.BasicTaskRunState;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunPhase;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunState;

public class TaskRunFailed extends BasicTaskRunState {


    public TaskRunFailed(Integer taskRunParse) {
        super(taskRunParse);
    }

    @Override
    protected TaskRunState onReschedule() {
        return new TaskRunCreated(TaskRunPhase.CREATED);
    }

    @Override
    protected TaskRunState onSkip() {
        return new TaskRunSkip(TaskRunPhase.SKIP);
    }
}
