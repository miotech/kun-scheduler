package com.miotech.kun.workflow.common.taskrun.state;

import com.miotech.kun.workflow.core.model.taskrun.BasicTaskRunState;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunPhase;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunState;

public class TaskRunSuccess extends BasicTaskRunState {


    public TaskRunSuccess(Integer taskRunParse) {
        super(taskRunParse);
    }

    @Override
    protected TaskRunState onReschedule() {
        return new TaskRunCreated(TaskRunPhase.CREATED);
    }
}
