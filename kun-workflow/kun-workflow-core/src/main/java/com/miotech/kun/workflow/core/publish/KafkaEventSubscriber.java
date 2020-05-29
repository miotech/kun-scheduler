package com.miotech.kun.workflow.core.publish;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.miotech.kun.workflow.core.event.Event;
import com.miotech.kun.workflow.core.event.EventReceiver;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Properties;

public class KafkaEventSubscriber implements EventSubscriber {
    private static Logger logger = LoggerFactory.getLogger(KafkaEventSubscriber.class);
    private KafkaConsumer<String, String> consumer;
    private String topic;

    public KafkaEventSubscriber(String topic, Properties kafkaConf){
        this.topic = topic;
        consumer = new KafkaConsumer<>(kafkaConf);
    }

    @Override
    public void subscribe(EventReceiver receiver) {
        // TODO: need implementation
        consumer.subscribe(Arrays.asList(topic));
        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(1000);
            for (ConsumerRecord<String, String> record : records) {
                Event event = null;
                try {
                    event = EventMapper.toEvent(record.value());
                } catch (JsonProcessingException e) {
                    logger.error("parse event failed", e);
                }
                receiver.onReceive(event);
            }
        }
    }

}
