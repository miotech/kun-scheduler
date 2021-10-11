package com.miotech.kun.workflow.executor.mock;

import com.miotech.kun.commons.pubsub.event.Event;
import com.miotech.kun.commons.pubsub.event.EventReceiver;
import com.miotech.kun.commons.pubsub.subscribe.EventSubscriber;

public class MockEventSubscriber implements EventSubscriber {

    private EventReceiver receiver;

    @Override
    public void subscribe(EventReceiver receiver) {
        this.receiver = receiver;
    }

    public void receiveEvent(Event event) {
        receiver.onReceive(event);
    }
}
