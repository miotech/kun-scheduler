package com.miotech.kun.workflow.core.publish;

import com.miotech.kun.commons.pubsub.event.Event;
import com.miotech.kun.commons.utils.Props;
import com.miotech.kun.metadata.core.model.dataset.DataStore;
import com.miotech.kun.workflow.core.event.LineageEvent;
import com.miotech.kun.workflow.core.model.lineage.ElasticSearchIndexStore;
import com.miotech.kun.workflow.core.pubsub.KafkaEventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class KafkaEventPublisherTest {

    Props kafkaConf;

    @BeforeEach
    public void setUp() throws Exception {

        kafkaConf = new Props();
        kafkaConf.put("bootstrap.servers", "localhost:9092");
        kafkaConf.put("acks", "all");
        kafkaConf.put("retries", 0);
        kafkaConf.put("batch.size", 16384);
        kafkaConf.put("linger.ms", 1);
        kafkaConf.put("buffer.memory", 33554432);
        kafkaConf.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        kafkaConf.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        kafkaConf.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        kafkaConf.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

        kafkaConf.put("group.id", "test-group");
        kafkaConf.put("enable.auto.commit", "true");
        kafkaConf.put("auto.commit.interval.ms", "1000");
    }

    @Test
    @Disabled
    public void publish() {

        KafkaEventPublisher publisher = new KafkaEventPublisher("test", kafkaConf);
        long taskId = 111;
        List<DataStore> inlets = new ArrayList<>();
        List<DataStore> outlets = new ArrayList<>();
        inlets.add(new ElasticSearchIndexStore("127.0.0.1", 9200, "test1"));
        outlets.add(new ElasticSearchIndexStore("127.0.0.1", 9200, "test2"));
        Event event = new LineageEvent(taskId, inlets, outlets);
        publisher.publish(event);
    }
}