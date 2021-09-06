package com.miotech.kun.workflow.common.task.vo;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.miotech.kun.workflow.core.model.common.Tag;
import com.miotech.kun.workflow.core.execution.Config;
import com.miotech.kun.workflow.core.model.task.ScheduleConf;
import com.miotech.kun.workflow.core.model.task.Task;

import java.util.List;
import java.util.stream.Collectors;

@JsonDeserialize(builder = TaskPropsVO.TaskPropsVOBuilder.class)
public class TaskPropsVO {
    private final String name;
    private final String description;
    private final Long operatorId;
    private final Config config;
    private final ScheduleConf scheduleConf;
    private final List<TaskDependencyVO> dependencies;
    private final List<Tag> tags;
    private final String queueName;
    private final String priority;
    private final Integer retries;
    private final Integer retryDelay;

    private TaskPropsVO(TaskPropsVOBuilder builder) {
        this.name = builder.name;
        this.description = builder.description;
        this.operatorId = builder.operatorId;
        this.config = builder.config;
        this.scheduleConf = builder.scheduleConf;
        this.dependencies = builder.dependencies;
        this.tags = builder.tags;
        this.queueName = builder.queueName;
        this.priority = builder.priority;
        this.retries = builder.retries;
        this.retryDelay = builder.retryDelay;
    }

    public static TaskPropsVOBuilder newBuilder() {
        return new TaskPropsVOBuilder();
    }

    public static TaskPropsVO from(Task task) {
        List<TaskDependencyVO> dependencies = task.getDependencies()
                .stream()
                .map(x -> TaskDependencyVO.newBuilder()
                        .withDependencyFunc(x.getDependencyFunction()
                                .toFunctionType())
                        .withDownstreamTaskId(x.getDownstreamTaskId())
                        .withUpstreamTaskId(x.getUpstreamTaskId())
                        .withDependencyLevel(x.getDependencyLevel().name())
                        .build())
                .collect(Collectors.toList());

        return new TaskPropsVOBuilder()
                .withName(task.getName())
                .withDescription(task.getDescription())
                .withOperatorId(task.getOperatorId())
                .withConfig(task.getConfig())
                .withScheduleConf(task.getScheduleConf())
                .withDependencies(dependencies)
                .withTags(task.getTags())
                .withQueueName(task.getQueueName())
                .build();
    }

    public TaskPropsVOBuilder cloneBuilder() {
        return new TaskPropsVOBuilder()
                .withName(name)
                .withDescription(description)
                .withOperatorId(operatorId)
                .withConfig(config)
                .withScheduleConf(scheduleConf)
                .withDependencies(dependencies)
                .withTags(tags);
    }

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

    public List<Tag> getTags() {
        return tags;
    }

    public List<TaskDependencyVO> getDependencies() {
        return dependencies;
    }

    public String getPriority() {
        return priority;
    }

    public String getQueueName() {
        return queueName;
    }

    public Integer getRetries() {
        return retries;
    }

    public Integer getRetryDelay() {
        return retryDelay;
    }

    @JsonPOJOBuilder
    public static final class TaskPropsVOBuilder {
        private String name;
        private String description;
        private Long operatorId;
        private Config config;
        private ScheduleConf scheduleConf;
        private List<TaskDependencyVO> dependencies;
        private List<Tag> tags;
        private String queueName;
        private String priority;
        private Integer retries;
        private Integer retryDelay;

        private TaskPropsVOBuilder() {
        }

        public TaskPropsVOBuilder withName(String name) {
            this.name = name;
            return this;
        }

        public TaskPropsVOBuilder withDescription(String description) {
            this.description = description;
            return this;
        }

        public TaskPropsVOBuilder withOperatorId(Long operatorId) {
            this.operatorId = operatorId;
            return this;
        }

        public TaskPropsVOBuilder withConfig(Config config) {
            this.config = config;
            return this;
        }

        public TaskPropsVOBuilder withScheduleConf(ScheduleConf scheduleConf) {
            this.scheduleConf = scheduleConf;
            return this;
        }

        public TaskPropsVOBuilder withDependencies(List<TaskDependencyVO> dependencies) {
            this.dependencies = dependencies;
            return this;
        }

        public TaskPropsVOBuilder withTags(List<Tag> tags) {
            this.tags = tags;
            return this;
        }

        public TaskPropsVOBuilder withPriority(String priority) {
            this.priority = priority;
            return this;
        }

        public TaskPropsVOBuilder withQueueName(String queueName){
            this.queueName = queueName;
            return this;
        }

        public TaskPropsVOBuilder withRetries(Integer retries){
            this.retries = retries;
            return this;
        }

        public TaskPropsVOBuilder withRetryDelay(Integer retryDelay){
            this.retryDelay = retryDelay;
            return this;
        }

        public TaskPropsVO build() {
            return new TaskPropsVO(this);
        }
    }
}
