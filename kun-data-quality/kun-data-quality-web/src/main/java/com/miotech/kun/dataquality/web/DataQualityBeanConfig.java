package com.miotech.kun.dataquality.web;

import com.miotech.kun.commons.pubsub.publish.EventPublisher;
import com.miotech.kun.commons.pubsub.subscribe.EventSubscriber;
import com.miotech.kun.dataquality.web.utils.WorkflowUtils;
import com.miotech.kun.workflow.client.model.Operator;
import com.miotech.kun.workflow.core.pubsub.RedisStreamEventPublisher;
import com.miotech.kun.workflow.core.pubsub.RedisStreamEventSubscriber;
import io.lettuce.core.RedisClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author: Jie Chen
 * @created: 2020/7/17
 */
@Configuration
@ConditionalOnProperty(value = "testenv", havingValue = "false", matchIfMissing = true)
public class DataQualityBeanConfig {

    @Value("${workflow.base-url:http://kun-workflow:8088}")
    String workflowUrl;

    @Value("${redis.host}")
    private String redisHost = null;

    @Value("${redis.stream-key}")
    private String streamKey;

    @Value("${redis.data-quality.group}")
    private String group;

    @Value("${redis.data-quality.consumer}")
    private String consumer;

    @Autowired
    WorkflowUtils workflowUtils;

    @Bean
    Operator getOperator() {
        return Operator.newBuilder()
                .withName(DataQualityConfiguration.WORKFLOW_OPERATOR_NAME)
                .withDescription("Data Quality Operator")
                .withClassName("com.miotech.kun.workflow.operator.DataQualityOperator")
                .build();
    }

    @Bean("dataQuality-subscriber")
    public EventSubscriber getRedisSubscriber() {
        RedisClient redisClient = RedisClient.create(String.format("redis://%s", redisHost));
        return new RedisStreamEventSubscriber(streamKey, group, consumer, redisClient);
    }

    @Bean("dataQuality-publisher")
    public EventPublisher getRedisPublisher() {
        RedisClient redisClient = RedisClient.create(String.format("redis://%s", redisHost));
        return new RedisStreamEventPublisher(streamKey, redisClient);
    }
}
