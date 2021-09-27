package com.miotech.kun.commons.pubsub.subscribe;

import com.miotech.kun.commons.pubsub.event.EventReceiver;

public class NopEventSubscriber implements EventSubscriber {
    @Override
    public void subscribe(EventReceiver receiver) {

    }
}
