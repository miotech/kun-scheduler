package com.miotech.kun.dataplatform.config;

import com.miotech.kun.dataplatform.notify.TaskAttemptStatusChangeEventSubscriber;
import com.miotech.kun.workflow.core.publish.EventSubscriber;
import com.miotech.kun.workflow.core.publish.RedisEventSubscriber;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;


@Configuration
public class EventNotifyConfig {
    @Value("${redis.host}")
    private String redisHost = null;

    @Value("${redis.notify-channel:kun-notify}")
    private String channel;

    @Bean
    public TaskAttemptStatusChangeEventSubscriber getSubscriber() {
        return new TaskAttemptStatusChangeEventSubscriber();
    }

    @Bean
    public EventSubscriber getRedisSubscriber() {
        JedisPool jedisPool = new JedisPool(new JedisPoolConfig(), redisHost);
        return new RedisEventSubscriber(channel, jedisPool);
    }
}
