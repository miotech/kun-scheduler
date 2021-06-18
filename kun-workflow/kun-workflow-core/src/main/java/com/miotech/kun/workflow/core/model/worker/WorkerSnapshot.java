package com.miotech.kun.workflow.core.model.worker;

import com.miotech.kun.workflow.core.model.taskrun.TaskRunStatus;

import java.time.OffsetDateTime;

public abstract class WorkerSnapshot {
    private final WorkerInstance ins;
    private final OffsetDateTime createdTime;

    public abstract TaskRunStatus getStatus();

    public WorkerSnapshot(WorkerInstance ins, OffsetDateTime createdTime) {
        this.ins = ins;
        this.createdTime = createdTime;
    }

    public WorkerInstance getIns() {
        return ins;
    }

    public OffsetDateTime getCreatedTime() {
        return createdTime;
    }

    @Override
    public String toString() {
        return "WorkerSnapshot{" +
                "ins=" + ins +
                ", createdTime=" + createdTime +
                ", status =" + getStatus() +
                '}';
    }
}
