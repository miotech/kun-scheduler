package com.miotech.kun.workflow.executor.local;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.miotech.kun.commons.pubsub.event.PublicEvent;
import com.miotech.kun.commons.pubsub.publish.EventPublisher;
import com.miotech.kun.commons.utils.InitializingBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class PublicEventHandler implements InitializingBean {
    private static final Logger logger = LoggerFactory.getLogger(PublicEventHandler.class);

    @Inject
    private EventBus eventBus;

    @Inject
    private EventPublisher publisher;

    public void init() {
        PublicEventListener listener = new PublicEventListener();
        eventBus.register(listener);
    }

    @Override
    public void afterPropertiesSet() {
        init();
    }

    private class PublicEventListener {
        @Subscribe
        public void publicEvent(PublicEvent event) {
            logger.debug("push public event = {}", event);
            publisher.publish(event);
        }
    }
}
