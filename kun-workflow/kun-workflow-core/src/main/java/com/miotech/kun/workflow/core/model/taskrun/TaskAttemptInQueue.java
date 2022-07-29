package com.miotech.kun.workflow.core.model.taskrun;

import java.time.OffsetDateTime;

public class TaskAttemptInQueue {
    private TaskAttempt taskAttempt;
    private OffsetDateTime enqueueTime;

    public TaskAttemptInQueue(TaskAttempt taskAttempt, OffsetDateTime enqueueTime) {
        this.taskAttempt = taskAttempt;
        this.enqueueTime = enqueueTime;
    }

    public TaskAttempt getTaskAttempt() {
        return taskAttempt;
    }

    public OffsetDateTime getEnqueueTime() {
        return enqueueTime;
    }
}
