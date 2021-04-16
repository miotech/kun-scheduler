package com.miotech.kun.dataplatform.notify;

import com.google.common.collect.Lists;
import com.miotech.kun.dataplatform.model.notify.TaskStatusNotifyTrigger;
import com.miotech.kun.dataplatform.notify.userconfig.NotifierUserConfig;

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
