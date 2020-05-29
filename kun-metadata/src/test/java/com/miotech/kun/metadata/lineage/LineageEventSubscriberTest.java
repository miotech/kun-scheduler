package com.miotech.kun.metadata.lineage;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Properties;

public class LineageEventSubscriberTest {

    Properties kafkaConf;


    @Before
    public void setUp() throws Exception {
        kafkaConf = new Properties();
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

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void subscribe() {
        Injector injector = Guice.createInjector();

        LineageEventSubscriber subscriber = new LineageEventSubscriber("test", kafkaConf);
        subscriber.subscribe();
    }

//    class LineageModule extends AbstractModule{
//        @Override
//        protected void configure(){
//            bind(SpellChecker.class).to(WinWordSpellChecker.class);
//        }
//    }
}