package com.miotech.kun.workflow.core.event;

import com.miotech.kun.workflow.core.model.common.Tick;

public class TickEvent extends Event {
    private final Tick tick;

    public TickEvent(Tick tick) {
        this.tick = tick;
    }
}
