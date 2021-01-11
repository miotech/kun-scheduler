package com.miotech.kun.metadata.databuilder.extract.tool;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

public class KafkaUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaUtil.class);
    private static final Map<String, KafkaProducer<String, String>> producerCache = new ConcurrentHashMap<>();

    /**
     * create producer
     *
     * @param brokers
     * @return
     */
    private static KafkaProducer<String, String> createProducer(String brokers) {
        Properties prop = new Properties();
        prop.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, brokers);
        prop.put(ProducerConfig.ACKS_CONFIG, "-1");
        prop.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        prop.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        return new KafkaProducer<>(prop);
    }


    /**
     * close producer
     */
    static {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> producerCache.forEach(KafkaUtil::close)));
    }


    private static KafkaProducer<String, String> getProducer(String brokers) {
        return producerCache.compute(brokers, (k, oldProducer) -> {
            if (oldProducer == null) {
                oldProducer = createProducer(brokers);
            }
            return oldProducer;
        });
    }


    /**
     * send message,need key
     *
     * @param topic
     * @param key
     * @param message
     * @return
     */
    public static Future<RecordMetadata> send(String brokers, String topic, String key, String message) {
        KafkaProducer<String, String> producer = getProducer(brokers);
        ProducerRecord<String, String> producerRecord = new ProducerRecord<String, String>(topic, key, message);
        return producer.send(producerRecord);
    }


    /**
     * send message,do not need key
     *
     * @param topic
     * @param message
     * @return
     */
    public static Future<RecordMetadata> send(String brokers, String topic, String message) {
        return send(brokers, topic, null, message);
    }


    /**
     * close method
     *
     * @param key
     * @param v
     */
    private static void close(String key, KafkaProducer<String, String> v) {
        try {
            v.close();
        } catch (Exception e) {
            LOGGER.error("Close fail", e);
        }
    }

}
