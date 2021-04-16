package com.miotech.kun.dataplatform.model.taskdefinition;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.miotech.kun.dataplatform.model.notify.TaskStatusNotifyTrigger;

import java.util.Collections;
import java.util.Map;


@JsonDeserialize(builder = TaskPayload.Builder.class)
public class TaskPayload {
    private final Map<String, Object> taskConfig;

    private final ScheduleConfig scheduleConfig;

    private final TaskDefNotifyConfig notifyConfig;

    public TaskPayload(Map<String, Object> taskConfig, ScheduleConfig scheduleConfig, TaskDefNotifyConfig notifyConfig) {
        this.taskConfig = taskConfig;
        this.scheduleConfig = scheduleConfig;
        this.notifyConfig = notifyConfig;
    }

    public Map<String, Object> getTaskConfig() {
        return taskConfig;
    }

    public ScheduleConfig getScheduleConfig() {
        return scheduleConfig;
    }

    public TaskDefNotifyConfig getNotifyConfig() {
        return notifyConfig;
    }

    public static Builder newBuilder() { return new Builder(); }

    public TaskPayload.Builder cloneBuilder() {
        return newBuilder()
                .withScheduleConfig(scheduleConfig)
                .withTaskConfig(taskConfig)
                .withNotifyConfig(notifyConfig);
    }

    @JsonPOJOBuilder
    public static final class Builder {
        private Map<String, Object> taskConfig;
        private ScheduleConfig scheduleConfig;
        private TaskDefNotifyConfig notifyConfig;

        private Builder() {
        }

        public Builder withTaskConfig(Map<String, Object> taskConfig) {
            this.taskConfig = taskConfig;
            return this;
        }

        public Builder withScheduleConfig(ScheduleConfig scheduleConfig) {
            this.scheduleConfig = scheduleConfig;
            return this;
        }

        public Builder withNotifyConfig(TaskDefNotifyConfig notifyConfig) {
            this.notifyConfig = notifyConfig;
            return this;
        }

        public TaskPayload build() {
            return new TaskPayload(taskConfig, scheduleConfig, notifyConfig);
        }
    }
}
