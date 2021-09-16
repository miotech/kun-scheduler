package com.miotech.kun.commons.pubsub.event;

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
