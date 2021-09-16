package com.miotech.kun.commons.pubsub.publish;

import com.miotech.kun.commons.pubsub.event.Event;

public interface EventPublisher {
    /**
     * workflow对外发送消息。
     */
    public void publish(Event event);
}
