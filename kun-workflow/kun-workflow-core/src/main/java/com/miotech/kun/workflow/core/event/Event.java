package com.miotech.kun.workflow.core.event;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import static com.fasterxml.jackson.annotation.JsonTypeInfo.As.PROPERTY;
import static com.fasterxml.jackson.annotation.JsonTypeInfo.Id.CLASS;

/**
 * General event base class.
 */
@JsonTypeInfo(use = CLASS, include = PROPERTY, property = "@class")
public abstract class Event {
    private final long timestamp;

    public Event() {
        this(System.currentTimeMillis());
    }

    public Event(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
