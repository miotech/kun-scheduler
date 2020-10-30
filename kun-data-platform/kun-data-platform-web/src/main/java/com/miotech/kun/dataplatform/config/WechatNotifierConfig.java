package com.miotech.kun.dataplatform.config;

import com.miotech.kun.dataplatform.notify.WechatNotifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WechatNotifierConfig {

    @Value("${zhongda.host}")
    private String host = null;

    @Value("${zhongda.token}")
    private String token;

    @Bean
    public WechatNotifier createWechatNotifier() {
        return new WechatNotifier(host, token);
    }

}
