package com.miotech.kun.metadata.web.kafka;

import java.util.Properties;

public abstract class EventProcessor {

    Properties generateGeneralConsumerProperties() {
        Properties properties = new Properties();
        properties.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        properties.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        properties.put("enable.auto.commit", "true");
        properties.put("auto.commit.interval.ms", "1000");
        properties.put("auto.offset.reset", "latest");
        properties.put("max.poll.records", "5");
        properties.put("max.poll.interval.ms", "600000");

        return properties;
    }

    public abstract void consume();

}
