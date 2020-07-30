package com.miotech.kun.dataplatform.model.taskdefinition;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.Map;

@JsonDeserialize(builder = TaskPayload.Builder.class)
public class TaskPayload {

    private final Map<String, Object> taskConfig;

    private final ScheduleConfig scheduleConfig;

    public TaskPayload(Map<String, Object> taskConfig, ScheduleConfig scheduleConfig) {
        this.taskConfig = taskConfig;
        this.scheduleConfig = scheduleConfig;
    }

    public Map<String, Object> getTaskConfig() {
        return taskConfig;
    }

    public ScheduleConfig getScheduleConfig() {
        return scheduleConfig;
    }

    public static Builder newBuilder() { return new Builder(); }

    public TaskPayload.Builder cloneBuilder() {
        return newBuilder()
                .withScheduleConfig(scheduleConfig)
                .withTaskConfig(taskConfig);
    }

    public static final class Builder {
        private Map<String, Object> taskConfig;
        private ScheduleConfig scheduleConfig;

        private Builder() {
        }

        public static Builder aTaskPayload() {
            return new Builder();
        }

        public Builder withTaskConfig(Map<String, Object> taskConfig) {
            this.taskConfig = taskConfig;
            return this;
        }

        public Builder withScheduleConfig(ScheduleConfig scheduleConfig) {
            this.scheduleConfig = scheduleConfig;
            return this;
        }

        public TaskPayload build() {
            return new TaskPayload(taskConfig, scheduleConfig);
        }
    }
}
