package com.miotech.kun.dataplatform.config;

import com.miotech.kun.dataplatform.notify.service.WeComService;
import com.miotech.kun.dataplatform.notify.service.ZhongdaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@ConditionalOnProperty(value = "testenv", havingValue = "false", matchIfMissing = true)
@Configuration
public class WeComServiceConfig {

    @Value("${notify.zhongda.host}")
    private String host = null;

    @Value("${notify.zhongda.token}")
    private String token;

    @Value("${notify.zhongda.notify-group}")
    private String group;

    @Value("${notify.urlLink.enabled:true}")
    private Boolean notifyUrlLinkEnabled;

    @Value("${notify.urlLink.prefix}")
    private String notifyUrlLinkPrefix;

    @Autowired
    private NotifyLinkConfig notifyLinkConfig;

    @Bean
    public ZhongdaService createZhongdaService() {
        return new ZhongdaService(host, token, group, notifyLinkConfig);
    }

    @Bean
    public WeComService createWecomService() {
        return new WeComService();
    }
}
