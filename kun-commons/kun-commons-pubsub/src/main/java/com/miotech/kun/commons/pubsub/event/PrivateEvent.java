package com.miotech.kun.commons.pubsub.event;

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
