package com.miotech.kun.datadiscovery.config;

import com.miotech.kun.commons.pubsub.subscribe.EventSubscriber;
import com.miotech.kun.workflow.core.pubsub.RedisStreamEventSubscriber;
import io.lettuce.core.RedisClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author: Jie Chen
 * @created: 2020/7/17
 */
@Configuration
@ConditionalOnExpression("${discovery.enabled:true}")
public class EventBeanConfig {

    @Value("${redis.host}")
    private String redisHost = null;

    @Value("${redis.metadata-stream-key}")
    private String streamKey;

    @Value("${redis.data-discovery.group}")
    private String group;

    @Value("${redis.data-discovery.consumer}")
    private String consumer;


    @Bean("data-discovery-subscriber")
    public EventSubscriber getRedisSubscriber() {
        RedisClient redisClient = RedisClient.create(String.format("redis://%s", redisHost));
        return new RedisStreamEventSubscriber(streamKey, group, consumer, redisClient);
    }

}
