package com.miotech.kun.workflow.common.taskrun.state;

import com.miotech.kun.workflow.core.model.taskrun.BasicTaskRunState;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunPhase;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunState;

public class TaskRunQueued extends BasicTaskRunState {

    public TaskRunQueued( Integer taskRunParse) {
        super(taskRunParse);
    }


    @Override
    protected TaskRunState onReady() {
        return new TaskRunRunning(TaskRunPhase.RUNNING);
    }

    @Override
    protected TaskRunState onException() {
        return new TaskRunFailed(TaskRunPhase.FAILED);
    }

    @Override
    protected TaskRunState onAbort() {
        return new TaskRunAborted(TaskRunPhase.ABORTED);
    }

    @Override
    protected TaskRunState onRecover() {
        return this;
    }
}
