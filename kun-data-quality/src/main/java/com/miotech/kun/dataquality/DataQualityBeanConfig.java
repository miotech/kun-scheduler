package com.miotech.kun.dataquality;

import com.miotech.kun.dataquality.utils.WorkflowUtils;
import com.miotech.kun.workflow.client.model.Operator;
import com.miotech.kun.workflow.core.publish.EventSubscriber;
import com.miotech.kun.workflow.core.publish.RedisEventSubscriber;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * @author: Jie Chen
 * @created: 2020/7/17
 */
@Configuration
public class DataQualityBeanConfig {

    @Value("${workflow.base-url:http://kun-workflow:8088}")
    String workflowUrl;

    @Value("${redis.host}")
    private String redisHost = null;

    @Value("${redis.notify-channel:kun-notify}")
    private String channel;

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

    @Bean
    public EventSubscriber getRedisSubscriber() {
        JedisPool jedisPool = new JedisPool(new JedisPoolConfig(), redisHost);
        return new RedisEventSubscriber(channel, jedisPool);
    }
}
