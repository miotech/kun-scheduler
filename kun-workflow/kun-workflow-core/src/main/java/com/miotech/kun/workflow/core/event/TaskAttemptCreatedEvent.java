package com.miotech.kun.workflow.core.event;

import java.util.List;

public class TaskAttemptCreatedEvent extends PublicEvent{

    private final List<Long> taskAttemptIds;

    public TaskAttemptCreatedEvent(long timestamp, List<Long> taskAttemptIds) {
        super(timestamp);
        this.taskAttemptIds = taskAttemptIds;
    }
}
