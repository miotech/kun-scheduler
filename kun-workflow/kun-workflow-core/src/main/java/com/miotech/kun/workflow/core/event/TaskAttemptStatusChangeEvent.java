package com.miotech.kun.workflow.core.event;

import com.miotech.kun.workflow.core.model.taskrun.TaskRunStatus;

public class TaskAttemptStatusChangeEvent extends Event {
    private final long attemptId;
    private final TaskRunStatus from;
    private final TaskRunStatus to;

    public TaskAttemptStatusChangeEvent(long attemptId, TaskRunStatus from, TaskRunStatus to) {
        this.attemptId = attemptId;
        this.from = from;
        this.to = to;
    }

    public long getAttemptId() {
        return attemptId;
    }

    public TaskRunStatus getFromStatus() {
        return from;
    }

    public TaskRunStatus getToStatus() {
        return to;
    }
}
