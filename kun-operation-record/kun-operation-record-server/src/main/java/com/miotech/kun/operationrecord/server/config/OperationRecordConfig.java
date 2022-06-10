package com.miotech.kun.operationrecord.server.config;

import com.miotech.kun.commons.pubsub.publish.EventPublisher;
import com.miotech.kun.commons.pubsub.subscribe.EventSubscriber;
import com.miotech.kun.workflow.core.pubsub.RedisStreamEventPublisher;
import com.miotech.kun.workflow.core.pubsub.RedisStreamEventSubscriber;
import io.lettuce.core.RedisClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;

@Configuration
@ConditionalOnProperty(value = "testenv", havingValue = "false", matchIfMissing = true)
public class OperationRecordConfig {

    @Value("${redis.host}")
    private String redisHost = null;

    @Value("${redis.notify-channel:kun-notify}")
    private String channel;

    @Value("${redis.stream-key}")
    private String streamKey;

    @Value("${redis.operation-record.group}")
    private String group;

    @Value("${redis.operation-record.consumer}")
    private String consumer;

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
        RedisClient redisClient = RedisClient.create(String.format("redis://%s", redisHost));
        return new RedisStreamEventPublisher(streamKey, redisClient);
    }

    @Bean("operationRecord-subscriber")
    public EventSubscriber getRedisSubscriber() {
        RedisClient redisClient = RedisClient.create(String.format("redis://%s", redisHost));
        return new RedisStreamEventSubscriber(streamKey, group, consumer, redisClient);
    }

}
