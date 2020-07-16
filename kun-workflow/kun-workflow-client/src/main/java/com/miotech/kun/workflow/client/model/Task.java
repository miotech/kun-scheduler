package com.miotech.kun.workflow.client.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.miotech.kun.workflow.core.execution.Config;
import com.miotech.kun.workflow.core.model.common.Tag;
import com.miotech.kun.workflow.core.model.task.ScheduleConf;
import com.miotech.kun.workflow.core.model.task.TaskDependency;

import java.util.List;

@JsonDeserialize(builder = Task.Builder.class)
public class Task {
    private final Long id;
    private final String name;
    private final String description;
    private final Long operatorId;
    private final Config config;
    private final ScheduleConf scheduleConf;
    private final List<TaskDependency> dependencies;
    private final List<Tag> tags;

    public Task(Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.description = builder.description;
        this.operatorId = builder.operatorId;
        this.config = builder.config;
        this.scheduleConf = builder.scheduleConf;
        this.dependencies = builder.dependencies;
        this.tags = builder.tags;

    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public Long getId() { return id; }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Long getOperatorId() {
        return operatorId;
    }

    public Config getConfig() {
        return config;
    }

    public ScheduleConf getScheduleConf() {
        return scheduleConf;
    }

    public List<TaskDependency> getDependencies() { return dependencies; }

    public List<Tag> getTags() { return tags; }

    @JsonPOJOBuilder
    public static final class Builder {
        private Long id;
        private String name;
        private String description;
        private Long operatorId;
        private Config config;
        private ScheduleConf scheduleConf;
        private List<TaskDependency> dependencies;
        private List<Tag> tags;

        private Builder() {
        }

        public Builder withId(Long id) {
            this.id = id;
            return this;
        }

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public Builder withDescription(String description) {
            this.description = description;
            return this;
        }

        public Builder withOperatorId(Long operatorId) {
            this.operatorId = operatorId;
            return this;
        }

        public Builder withConfig(Config config) {
            this.config = config;
            return this;
        }

        public Builder withScheduleConf(ScheduleConf scheduleConf) {
            this.scheduleConf = scheduleConf;
            return this;
        }

        public Builder withDependencies(List<TaskDependency> dependencies) {
            this.dependencies = dependencies;
            return this;
        }

        public Builder withTags(List<Tag> tags) {
            this.tags = tags;
            return this;
        }

        public Task build() {
            return new Task(this);
        }
    }
}