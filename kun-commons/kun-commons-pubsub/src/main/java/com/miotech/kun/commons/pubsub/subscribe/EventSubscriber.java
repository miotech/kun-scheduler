package com.miotech.kun.commons.pubsub.subscribe;

import com.miotech.kun.commons.pubsub.event.EventReceiver;

public interface EventSubscriber {
    /**
     * 订阅workflow发出的消息。
     */
    public void subscribe(EventReceiver receiver);
}
