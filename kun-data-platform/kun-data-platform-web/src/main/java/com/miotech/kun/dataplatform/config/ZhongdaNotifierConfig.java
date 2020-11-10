package com.miotech.kun.dataplatform.config;

import com.miotech.kun.dataplatform.notify.ZhongdaNotifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ZhongdaNotifierConfig {

    @Value("${zhongda.host}")
    private String host = null;

    @Value("${zhongda.token}")
    private String token;

    @Value("${zhongda.notify-group}")
    private String group;

    @Bean
    public ZhongdaNotifier createWechatNotifier() {
        return new ZhongdaNotifier(host, token, group);
    }

}
