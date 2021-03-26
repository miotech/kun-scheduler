package com.miotech.kun.workflow.core.model.worker;

import com.miotech.kun.workflow.core.model.taskrun.TaskRunStatus;

import java.util.Date;

public abstract class WorkerSnapshot {
    private final WorkerInstance ins;
    private final Date createdTime;
    public abstract TaskRunStatus getStatus();

    public WorkerSnapshot(WorkerInstance ins,Date createdTime) {
        this.ins = ins;
        this.createdTime = createdTime;
    }

    public WorkerInstance getIns() {
        return ins;
    }


}
