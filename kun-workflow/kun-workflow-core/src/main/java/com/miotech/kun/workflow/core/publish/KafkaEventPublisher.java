package com.miotech.kun.workflow.core.publish;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.inject.Singleton;
import com.miotech.kun.commons.utils.Props;
import com.miotech.kun.workflow.core.event.Event;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Singleton
public class KafkaEventPublisher implements EventPublisher {

    private static Logger logger = LoggerFactory.getLogger(KafkaEventPublisher.class);
    private Producer<String, String> producer;
    private String topic;

    public KafkaEventPublisher(String topic, Props props){
        producer = new KafkaProducer<String, String>(props.toProperties());
        this.topic = topic;
    }

    @Override
    public void publish(Event event) {
        ProducerRecord record = null;
        try {
            record = new ProducerRecord<String, String>(topic, "", EventMapper.toJson(event));
        } catch (JsonProcessingException e) {
            logger.error("convert kafka event msg", e);
        }
        Future<RecordMetadata> result =  producer.send(record);
        try {
            result.get(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            logger.error("send msg to kafka failed", e);
        } catch (ExecutionException e) {
            logger.error("send msg to kafka failed", e);
        } catch (TimeoutException e) {
            logger.error("send msg to kafka failed", e);
        }
    }
}
