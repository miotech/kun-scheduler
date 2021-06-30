package com.miotech.kun.workflow.core.event;

import com.miotech.kun.workflow.core.model.common.Tick;

public class TickEvent extends PrivateEvent {
    private final Tick tick;

    public TickEvent(Tick tick) {
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
