package com.miotech.kun.workflow.core.model.common;

import com.miotech.kun.workflow.core.model.taskrun.TaskRunStatus;

public abstract class WorkerSnapshot {
    private WorkerInstance ins;
    public abstract TaskRunStatus getStatus();
}
