package com.miotech.kun.workflow.core.model.taskrun;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

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

    private static final Map<String, TaskRunStatus> mappings = new HashMap<>(16);

    static {
        for (TaskRunStatus status : values()) {
            mappings.put(status.name(), status);
        }
    }

    @Nullable
    public static TaskRunStatus resolve(@Nullable String status) {
        return (status != null ? mappings.get(status) : null);
    }

    public boolean matches(String status) {
        return (this == resolve(status));
    }

    public boolean isSuccess() {
        return this == SUCCESS || this == SKIPPED;
    }

    public boolean isFailure() {
        return this == FAILED || this == ABORTED;
    }

    public boolean isFinished() {
        return isSuccess() || isFailure() || isAborted();
    }

    public boolean isAborted() {
        return this == ABORTED;
    }

    public boolean isSkipped() {
        return this == SKIPPED;
    }
}
