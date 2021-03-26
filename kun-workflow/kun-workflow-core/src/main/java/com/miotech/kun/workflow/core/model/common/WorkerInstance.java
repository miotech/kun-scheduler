package com.miotech.kun.workflow.core.model.common;

public class WorkerInstance{
    private final long taskAttemptId;
    private final String workerId;

    public WorkerInstance(long taskAttemptId, String workerId) {
        this.taskAttemptId = taskAttemptId;
        this.workerId = workerId;
    }

    public long getTaskAttemptId() {
        return taskAttemptId;
    }

    public String getWorkerId() {
        return workerId;
    }

}
