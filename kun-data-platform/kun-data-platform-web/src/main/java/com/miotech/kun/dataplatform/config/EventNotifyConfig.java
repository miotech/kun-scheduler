package com.miotech.kun.dataplatform.config;

import com.miotech.kun.dataplatform.notify.WorkflowEventDispatcher;
import com.miotech.kun.workflow.core.publish.EventSubscriber;
import com.miotech.kun.workflow.core.publish.RedisEventSubscriber;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;


@ConditionalOnProperty(value = "testenv", havingValue = "false", matchIfMissing = true)
@Configuration
public class EventNotifyConfig {
    @Value("${redis.host}")
    private String redisHost = null;

    @Value("${redis.notify-channel:kun-notify}")
    private String channel;

    @Bean
    public WorkflowEventDispatcher createWorkflowEventDispatcher() {
        return new WorkflowEventDispatcher();
    }

    @Bean
    public EventSubscriber getRedisSubscriber() {
        JedisPool jedisPool = new JedisPool(new JedisPoolConfig(), redisHost);
        return new RedisEventSubscriber(channel, jedisPool);
    }
}
