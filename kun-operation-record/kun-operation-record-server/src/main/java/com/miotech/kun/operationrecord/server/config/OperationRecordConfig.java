package com.miotech.kun.operationrecord.server.config;

import com.miotech.kun.commons.pubsub.publish.EventPublisher;
import com.miotech.kun.commons.pubsub.subscribe.EventSubscriber;
import com.miotech.kun.workflow.core.pubsub.RedisEventPublisher;
import com.miotech.kun.workflow.core.pubsub.RedisEventSubscriber;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

@Configuration
@ConditionalOnProperty(value = "testenv", havingValue = "false", matchIfMissing = true)
public class OperationRecordConfig {

    @Value("${redis.host}")
    private String redisHost = null;

    @Value("${redis.notify-channel:kun-notify}")
    private String channel;

    @Bean
    public ExpressionParser getExpressionParser() {
        return new SpelExpressionParser();
    }

    @Bean("localVariableTableParameterNameDiscoverer")
    public ParameterNameDiscoverer getParameterNameDiscoverer() {
        return new LocalVariableTableParameterNameDiscoverer();
    }

    @Bean("operationRecord-publisher")
    public EventPublisher getRedisPublisher() {
        JedisPool jedisPool = new JedisPool(new JedisPoolConfig(), redisHost);
        return new RedisEventPublisher(channel, jedisPool);
    }

    @Bean("operationRecord-subscriber")
    public EventSubscriber getRedisSubscriber() {
        JedisPool jedisPool = new JedisPool(new JedisPoolConfig(), redisHost);
        return new RedisEventSubscriber(channel, jedisPool);
    }

}
