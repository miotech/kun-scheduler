package com.miotech.kun.workflow.core.publish;

import com.miotech.kun.workflow.core.event.EventReceiver;

import java.util.Properties;

public class KafkaEventSubscriber implements EventSubscriber {
    private Properties kafkaConf;

    @Override
    public void subscribe(EventReceiver receiver) {
        // TODO: need implementation
    }
}
