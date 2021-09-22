package com.miotech.kun.monitor.alert.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Component
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

    public String getScheduledTaskLinkURL(Long taskDefinitionId, Long taskRunId) {
        return this.getPrefix() + String.format("/operation-center/scheduled-tasks/%s?taskRunId=%s", taskDefinitionId, taskRunId);
    }
}
