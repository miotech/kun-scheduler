package com.miotech.kun.workflow.common.taskrun.state;

import com.miotech.kun.workflow.core.model.taskrun.BasicTaskRunState;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunPhase;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunState;

public class TaskRunRunning extends BasicTaskRunState {

    public TaskRunRunning( Integer taskRunParse) {
        super(taskRunParse);
    }

    @Override
    protected TaskRunState onCheck() {
        return new TaskRunCheck(TaskRunPhase.CHECKING);
    }

    @Override
    protected TaskRunState onFailed() {
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
