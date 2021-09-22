package com.miotech.kun.monitor.facade.model.alert;

import com.google.common.collect.Lists;

import java.util.List;

public class SystemDefaultNotifierConfig {
    private List<NotifierUserConfig> systemDefaultConfig;

    private TaskStatusNotifyTrigger systemDefaultTriggerType;

    public SystemDefaultNotifierConfig(TaskStatusNotifyTrigger systemDefaultTriggerType, List<NotifierUserConfig> systemDefaultConfig) {
        this.systemDefaultTriggerType = systemDefaultTriggerType;
        this.systemDefaultConfig = (systemDefaultConfig == null) ? Lists.newArrayList() : systemDefaultConfig;
    }

    public TaskStatusNotifyTrigger getSystemDefaultTriggerType() {
        return systemDefaultTriggerType;
    }

    public List<NotifierUserConfig> getSystemDefaultConfig() {
        return systemDefaultConfig;
    }
}
