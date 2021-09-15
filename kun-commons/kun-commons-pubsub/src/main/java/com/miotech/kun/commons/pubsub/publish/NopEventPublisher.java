package com.miotech.kun.commons.pubsub.publish;

import com.miotech.kun.commons.pubsub.event.Event;

public class NopEventPublisher implements EventPublisher {
    @Override
    public void publish(Event event) {
        // nop
    }
}
