package com.miotech.kun.workflow.core.publish;

import com.miotech.kun.workflow.core.event.Event;

public interface EventPublisher {
    /**
     * workflow对外发送消息。
     */
    public void publish(Event event);
}
