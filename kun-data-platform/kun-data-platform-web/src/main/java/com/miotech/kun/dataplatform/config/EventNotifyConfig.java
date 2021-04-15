package com.miotech.kun.dataplatform.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.miotech.kun.dataplatform.model.notify.TaskStatusNotifyTrigger;
import com.miotech.kun.dataplatform.notify.SystemDefaultNotifierConfig;
import com.miotech.kun.dataplatform.notify.userconfig.NotifierUserConfig;
import com.miotech.kun.workflow.core.publish.EventSubscriber;
import com.miotech.kun.workflow.core.publish.RedisEventSubscriber;
import com.miotech.kun.workflow.utils.JSONUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.List;
import java.util.Objects;

@Configuration
@ConditionalOnProperty(value = "testenv", havingValue = "false", matchIfMissing = true)
public class EventNotifyConfig {
    @Value("${redis.host}")
    private String redisHost = null;

    @Value("${redis.notify-channel:kun-notify}")
    private String channel;

    @Value("${notify.systemDefault.triggerType:ON_FAIL}")
    private String systemDefaultConfigTriggerTypeStr;

    @Value("${notify.systemDefault.userConfigJson:[{\"notifierType\":\"WECOM\"}]}")
    private String systemDefaultNotifierConfigJson;

    @Bean
    public EventSubscriber getRedisSubscriber() {
        JedisPool jedisPool = new JedisPool(new JedisPoolConfig(), redisHost);
        return new RedisEventSubscriber(channel, jedisPool);
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
