package com.miotech.kun.workflow.core.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.miotech.kun.commons.pubsub.event.PrivateEvent;
import com.miotech.kun.workflow.core.model.common.Tick;

public class TickEvent extends PrivateEvent {
    private final Tick tick;

    @JsonCreator
    public TickEvent(@JsonProperty("tick") Tick tick) {
        this.tick = tick;
    }

    public Tick getTick() {
        return tick;
    }

    @Override
    public String toString() {
        return "TickEvent{" +
                "tick=" + tick +
                '}';
    }
}
