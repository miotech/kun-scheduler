package com.miotech.kun.workflow.core.event;

import com.miotech.kun.workflow.core.model.taskrun.TaskRunStatus;

public class TaskAttemptStatusChangeEvent extends Event {
    private final long taskAttemptId;
    private final TaskRunStatus from;
    private final TaskRunStatus to;

    public TaskAttemptStatusChangeEvent(long taskAttemptId, TaskRunStatus from, TaskRunStatus to) {
        this.taskAttemptId = taskAttemptId;
        this.from = from;
        this.to = to;
    }

    public long getTaskAttemptId() {
        return taskAttemptId;
    }

    public TaskRunStatus getFromStatus() {
        return from;
    }

    public TaskRunStatus getToStatus() {
        return to;
    }
}
