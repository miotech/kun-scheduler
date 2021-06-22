package com.miotech.kun.workflow.core.event;

public class TaskAttemptCreatedEvent extends PublicEvent{

    public TaskAttemptCreatedEvent(long timestamp) {
        super(timestamp);
    }
}
