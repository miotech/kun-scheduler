package com.miotech.kun.monitor.facade.model.alert;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collections;
import java.util.List;

public class TaskDefNotifyConfig {
    @JsonProperty("notifyWhen")
    private final TaskStatusNotifyTrigger notifyWhen;

    @JsonProperty("notifierConfig")
    private final List<NotifierUserConfig> notifierUserConfigList;

    public static final TaskDefNotifyConfig DEFAULT_TASK_NOTIFY_CONFIG = new TaskDefNotifyConfig(
            TaskStatusNotifyTrigger.SYSTEM_DEFAULT,
            Collections.emptyList()
    );

    public TaskDefNotifyConfig(
            @JsonProperty("notifyWhen") TaskStatusNotifyTrigger notifyWhen,
            @JsonProperty("notifierConfig") List<NotifierUserConfig> notifierUserConfigList
    ) {
        this.notifyWhen = notifyWhen;
        this.notifierUserConfigList = notifierUserConfigList;
    }

    public TaskStatusNotifyTrigger getNotifyWhen() {
        return notifyWhen;
    }

    public List<NotifierUserConfig> getNotifierUserConfigList() {
        return notifierUserConfigList;
    }
}
