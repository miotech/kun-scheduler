package com.miotech.kun.workflow.executor.local;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.miotech.kun.commons.pubsub.event.PublicEvent;
import com.miotech.kun.commons.pubsub.publish.EventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class MiscService {
    private static final Logger logger = LoggerFactory.getLogger(MiscService.class);

    @Inject
    private EventBus eventBus;

    @Inject
    private EventPublisher publisher;

    @Inject
    public void init() {
        PublicEventListener listener = new PublicEventListener();
        eventBus.register(listener);
    }

    private class PublicEventListener {
        @Subscribe
        public void publicEvent(PublicEvent event) {
            logger.debug("push public event = {}", event);
            publisher.publish(event);
        }
    }
}
