package com.miotech.kun.monitor.sla.config;

import com.miotech.kun.commons.pubsub.subscribe.EventSubscriber;
import com.miotech.kun.workflow.core.pubsub.RedisStreamEventSubscriber;
import io.lettuce.core.RedisClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(value = "testenv", havingValue = "false", matchIfMissing = true)
public class SlaEventNotifyConfig {
    @Value("${redis.host}")
    private String redisHost = null;

    @Value("${redis.stream-key}")
    private String streamKey;

    @Value("${redis.sla.group}")
    private String group;

    @Value("${redis.sla.consumer}")
    private String consumer;

    @Bean("sla-subscriber")
    public EventSubscriber getRedisSubscriber() {
        RedisClient redisClient = RedisClient.create(String.format("redis://%s", redisHost));
        return new RedisStreamEventSubscriber(streamKey, group, consumer, redisClient);
    }

}
