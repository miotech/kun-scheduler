package com.miotech.kun.monitor.alert.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.miotech.kun.commons.pubsub.subscribe.EventSubscriber;
import com.miotech.kun.monitor.facade.model.alert.NotifierUserConfig;
import com.miotech.kun.monitor.facade.model.alert.SystemDefaultNotifierConfig;
import com.miotech.kun.monitor.facade.model.alert.TaskStatusNotifyTrigger;
import com.miotech.kun.workflow.core.pubsub.RedisStreamEventSubscriber;
import com.miotech.kun.workflow.utils.JSONUtils;
import io.lettuce.core.RedisClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Objects;

@Configuration
@ConditionalOnProperty(value = "testenv", havingValue = "false", matchIfMissing = true)
public class AlertEventNotifyConfig {
    @Value("${redis.host}")
    private String redisHost = null;

    @Value("${redis.stream-key}")
    private String streamKey;

    @Value("${redis.alert.group}")
    private String group;

    @Value("${redis.alert.consumer}")
    private String consumer;

    @Value("${notify.systemDefault.triggerType:ON_FAIL}")
    private String systemDefaultConfigTriggerTypeStr;

    @Value("${notify.systemDefault.userConfigJson:[{\"notifierType\":\"EMAIL\"}]}")
    private String systemDefaultNotifierConfigJson;

    @Bean("alert-subscriber")
    public EventSubscriber getRedisSubscriber() {
        RedisClient redisClient = RedisClient.create(String.format("redis://%s", redisHost));
        return new RedisStreamEventSubscriber(streamKey, group, consumer, redisClient);
    }

    @Bean
    public SystemDefaultNotifierConfig getSystemDefaultNotifierConfig() {
        if (Objects.equals(systemDefaultConfigTriggerTypeStr, "SYSTEM_DEFAULT")) {
            throw new IllegalArgumentException("Cannot assign system default notify configuration trigger type to \"SYSTEM_DEFAULT\". Please change your configuration and restart!");
        }
        TaskStatusNotifyTrigger systemDefaultTriggerType = TaskStatusNotifyTrigger.from(this.systemDefaultConfigTriggerTypeStr);
        List<NotifierUserConfig> systemDefaultNotifierConfig =
                JSONUtils.jsonToObject(this.systemDefaultNotifierConfigJson, new TypeReference<List<NotifierUserConfig>>() {});
        return new SystemDefaultNotifierConfig(systemDefaultTriggerType, systemDefaultNotifierConfig);
    }
}
