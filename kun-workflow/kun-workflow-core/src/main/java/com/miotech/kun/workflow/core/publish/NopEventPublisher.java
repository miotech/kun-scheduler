package com.miotech.kun.workflow.core.publish;

import com.miotech.kun.workflow.core.event.Event;

public class NopEventPublisher implements EventPublisher {
    @Override
    public void publish(Event event) {
        // nop
    }
}
