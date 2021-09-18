package com.miotech.kun.workflow.core.event;

public class CheckResultEvent {
    private final long taskRunId;
    private final boolean checkStatus;

    public CheckResultEvent(long taskRunId, boolean checkStatus) {
        this.taskRunId = taskRunId;
        this.checkStatus = checkStatus;
    }

    public long getTaskRunId() {
        return taskRunId;
    }

    public boolean getCheckStatus() {
        return checkStatus;
    }
}
