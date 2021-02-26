package com.miotech.kun.dataplatform.model.notify;

import com.google.common.collect.ImmutableList;
import com.miotech.kun.dataplatform.notify.userconfig.NotifierUserConfig;
import com.miotech.kun.workflow.core.event.Event;

import java.util.List;

public class TaskNotifyConfig extends NotifyConfig {

    private final Long id;

    private final Long workflowTaskId;

    private final TaskStatusNotifyTrigger triggerType;

    private final List<NotifierUserConfig> notifierConfigs;

    @Override
    public boolean test(Event event) {
        // TODO
        return false;
    }

    private TaskNotifyConfig(Long id, Long workflowTaskId, TaskStatusNotifyTrigger triggerType, List<NotifierUserConfig> notifierUserConfigList) {
        this.id = id;
        this.workflowTaskId = workflowTaskId;
        this.triggerType = triggerType;
        this.notifierConfigs = ImmutableList.copyOf(notifierUserConfigList);
    }

    public Long getId() {
        return id;
    }

    public Long getWorkflowTaskId() {
        return workflowTaskId;
    }

    public TaskStatusNotifyTrigger getTriggerType() {
        return triggerType;
    }

    @Override
    public List<NotifierUserConfig> getNotifierConfigs() {
        return notifierConfigs;
    }

    public static TaskNotifyConfigBuilder newBuilder() {
        return new TaskNotifyConfigBuilder();
    }

    public TaskNotifyConfigBuilder cloneBuilder() {
        TaskNotifyConfigBuilder builder = new TaskNotifyConfigBuilder();
        builder.id = id;
        builder.workflowTaskId = workflowTaskId;
        builder.triggerType = triggerType;
        builder.notifierConfigs = notifierConfigs;
        return builder;
    }

    public static final class TaskNotifyConfigBuilder {
        private Long id;
        private Long workflowTaskId;
        private TaskStatusNotifyTrigger triggerType;
        private List<NotifierUserConfig> notifierConfigs;

        private TaskNotifyConfigBuilder() {
        }

        public TaskNotifyConfigBuilder withId(Long id) {
            this.id = id;
            return this;
        }

        public TaskNotifyConfigBuilder withWorkflowTaskId(Long workflowTaskId) {
            this.workflowTaskId = workflowTaskId;
            return this;
        }

        public TaskNotifyConfigBuilder withTriggerType(TaskStatusNotifyTrigger triggerType) {
            this.triggerType = triggerType;
            return this;
        }

        public TaskNotifyConfigBuilder withNotifierConfigs(List<NotifierUserConfig> notifierConfigs) {
            this.notifierConfigs = notifierConfigs;
            return this;
        }

        public TaskNotifyConfig build() {
            return new TaskNotifyConfig(id, workflowTaskId, triggerType, this.notifierConfigs);
        }
    }
}
