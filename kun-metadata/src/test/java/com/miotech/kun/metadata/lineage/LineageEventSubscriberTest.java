package com.miotech.kun.metadata.lineage;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.miotech.kun.metadata.testbase.DatabaseTestBase;
import com.miotech.kun.workflow.core.publish.EventSubscriber;
import com.miotech.kun.workflow.core.publish.KafkaEventSubscriber;
import org.junit.After;
import org.junit.Test;

import javax.inject.Inject;
import java.util.Properties;

public class LineageEventSubscriberTest extends DatabaseTestBase {

    @Inject
    LineageEventSubscriber subscriber;


    @Override
    protected void configuration() {
        super.configuration();
        addModules(new ProviderModule());
    }

    @After
    public void tearDown()  {
    }

    @Test
    public void subscribe() {

        subscriber.subscribe();
    }


    public static class ProviderModule extends AbstractModule {
        @Provides
        @Singleton
        public EventSubscriber createEventSubscriber() {

            Properties kafkaConf = new Properties();
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

            return new KafkaEventSubscriber("test", kafkaConf);
        }
    }

}