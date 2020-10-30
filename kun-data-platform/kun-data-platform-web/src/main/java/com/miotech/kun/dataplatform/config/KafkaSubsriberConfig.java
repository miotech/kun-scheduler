package com.miotech.kun.dataplatform.config;

import com.miotech.kun.commons.utils.Props;
import com.miotech.kun.workflow.core.publish.KafkaEventSubscriber;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class KafkaSubsriberConfig {

    @Value("${kafka.base-url}")
    private String kafkaUrl = null;

    @Value("${kafka.notify-group}")
    private String kafkaGroup = null;

    @Value("${kafka.notify-topic}")
    private String kafkaTopic = null;

    @Bean
    public KafkaEventSubscriber createKafkaSubscriber(){
        Props kafkaConf = new Props();
        kafkaConf.put("bootstrap.servers", kafkaUrl);
        kafkaConf.put("acks", "all");
        kafkaConf.put("retries", 0);
        kafkaConf.put("batch.size", 16384);
        kafkaConf.put("linger.ms", 1);
        kafkaConf.put("buffer.memory", 33554432);
        kafkaConf.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        kafkaConf.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        kafkaConf.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        kafkaConf.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

        kafkaConf.put("group.id", kafkaGroup);
        kafkaConf.put("enable.auto.commit", "true");
        kafkaConf.put("auto.commit.interval.ms", "1000");

        return new KafkaEventSubscriber(kafkaTopic, kafkaConf);
    }
}
