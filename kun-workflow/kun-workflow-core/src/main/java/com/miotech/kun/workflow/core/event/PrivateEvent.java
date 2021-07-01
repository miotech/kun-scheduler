package com.miotech.kun.workflow.core.event;

/**
 * An internal event.
 */
public class PrivateEvent extends Event {
    public PrivateEvent() {
        super();
    }

    public PrivateEvent(long timestamp) {
        super(timestamp);
    }
}
