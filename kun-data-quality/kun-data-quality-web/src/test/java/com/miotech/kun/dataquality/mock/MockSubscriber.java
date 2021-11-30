package com.miotech.kun.dataquality.mock;

import com.miotech.kun.commons.pubsub.event.Event;
import com.miotech.kun.commons.pubsub.event.EventReceiver;
import com.miotech.kun.commons.pubsub.subscribe.EventSubscriber;

public class MockSubscriber implements EventSubscriber {

    private EventReceiver receiver;

    public void post(Event event) {
        receiver.post(event);
    }

    @Override
    public void subscribe(EventReceiver receiver) {
        this.receiver = receiver;
    }

    public void receiveEvent(Event event){
        receiver.onReceive(event);
    }
}
