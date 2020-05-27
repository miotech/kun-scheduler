package com.miotech.kun.workflow.core.publish;

import com.miotech.kun.workflow.core.event.Event;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;

import java.util.Properties;

public class KafkaEventPublisher implements EventPublisher {

    private String topic;

    @Override
    public void publish(Event event) {
        // TODO: need implementation
        Properties props = new Properties();

        props.put("bootstrap.servers", "localhost:9092");
        props.put("acks", "all");
        props.put("retries", 0);
        props.put("batch.size", 16384);
        props.put("linger.ms", 1);
        props.put("buffer.memory", 33554432);

        Producer<String, String> producer = new KafkaProducer
                <String, String>(props);

    }
}
