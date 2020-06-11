package com.miotech.kun.workflow.core.publish;

import com.miotech.kun.workflow.core.event.EventReceiver;

public interface EventSubscriber {
    /**
     * 订阅workflow发出的消息。
     */
    public void subscribe(EventReceiver receiver);
}
