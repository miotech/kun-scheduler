package com.miotech.kun.dataplatform.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Configuration
public class NotifyLinkConfig {
    @Value("${notify.urlLink.enabled:true}")
    private boolean enabled;

    @Value("${notify.urlLink.prefix}")
    private String prefix;

    public boolean isEnabled() {
        return enabled;
    }

    public String getPrefix() {
        return prefix;
    }
}
