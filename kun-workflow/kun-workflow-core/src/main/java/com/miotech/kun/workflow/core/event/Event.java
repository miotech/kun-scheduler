package com.miotech.kun.workflow.core.event;

public abstract class Event {
    private final long timestamp;

    public Event(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
