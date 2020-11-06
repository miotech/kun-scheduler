package com.miotech.kun.workflow.core.publish;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.miotech.kun.commons.utils.Props;

public class KafkaModule extends AbstractModule{
    private final Props props;

    public KafkaModule(Props props) {
        this.props = props;
    }

    @Provides
    public KafkaEventPublisher createKafkaPublisher() {
        Props kafkaConf = new Props();
        kafkaConf.put("bootstrap.servers", props.getString("kafka.base-url"));
        kafkaConf.put("acks", "all");
        kafkaConf.put("retries", 0);
        kafkaConf.put("batch.size", 16384);
        kafkaConf.put("linger.ms", 1);
        kafkaConf.put("buffer.memory", 33554432);
        kafkaConf.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        kafkaConf.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        kafkaConf.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        kafkaConf.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

        kafkaConf.put("group.id", props.getString("kafka.notify-group"));
        kafkaConf.put("enable.auto.commit", "true");
        kafkaConf.put("auto.commit.interval.ms", "1000");
        return new KafkaEventPublisher(props.getString("kafka.notify-topic"), kafkaConf);
    }
}
