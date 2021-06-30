package com.miotech.kun.workflow.core.event;

/**
 * A public event should be delivered to external systems.
 */
public class PublicEvent extends Event {
    public PublicEvent(long timestamp) {
        super(timestamp);
    }

    public PublicEvent() {
        super();
    }
}
