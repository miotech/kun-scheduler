package com.miotech.kun.workflow.core.model.taskrun;

public enum TaskRunStatus {
    CREATED,
    QUEUED,
    RUNNING,
    SUCCESS,
    FAILED,
    RETRY,
    SKIPPED,
    ABORTING,
    ABORTED;

    public boolean isSuccess() {
        return this == SUCCESS || this == SKIPPED;
    }
}
