package com.miotech.kun.workflow.common.task.vo;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.miotech.kun.workflow.core.model.common.Param;
import com.miotech.kun.workflow.core.model.common.Tag;
import com.miotech.kun.workflow.core.model.common.Variable;
import com.miotech.kun.workflow.core.model.task.ScheduleConf;
import com.miotech.kun.workflow.core.model.task.Task;
import com.miotech.kun.workflow.core.model.task.TaskDependency;

import java.util.List;

@JsonDeserialize(builder = TaskPropsVO.TaskPropsVOBuilder.class)
public class TaskPropsVO {
    private final String name;
    private final String description;
    private final Long operatorId;
    private final List<Param> arguments;
    private final List<Variable> variableDefs;
    private final ScheduleConf scheduleConf;
    private final List<TaskDependency> dependencies;
    private final List<Tag> tags;

    private TaskPropsVO(TaskPropsVOBuilder builder) {
        this.name = builder.name;
        this.description = builder.description;
        this.operatorId = builder.operatorId;
        this.arguments = builder.arguments;
        this.variableDefs = builder.variableDefs;
        this.scheduleConf = builder.scheduleConf;
        this.dependencies = builder.dependencies;
        this.tags = builder.tags;
    }

    public static TaskPropsVOBuilder newBuilder() {
        return new TaskPropsVOBuilder();
    }

    public static TaskPropsVO from(Task task) {
        return new TaskPropsVOBuilder()
                .withName(task.getName())
                .withDescription(task.getDescription())
                .withOperatorId(task.getOperatorId())
                .withArguments(task.getArguments())
                .withVariableDefs(task.getVariableDefs())
                .withScheduleConf(task.getScheduleConf())
                .withDependencies(task.getDependencies())
                .withTags(task.getTags())
                .build();
    }

    public TaskPropsVOBuilder cloneBuilder() {
        return new TaskPropsVOBuilder()
                .withName(name)
                .withDescription(description)
                .withOperatorId(operatorId)
                .withArguments(arguments)
                .withVariableDefs(variableDefs)
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

    public List<Param> getArguments() {
        return arguments;
    }

    public List<Variable> getVariableDefs() {
        return variableDefs;
    }

    public ScheduleConf getScheduleConf() {
        return scheduleConf;
    }

    public List<Tag> getTags() {
        return tags;
    }

    public List<TaskDependency> getDependencies() { return dependencies; }

    @JsonPOJOBuilder
    public static final class TaskPropsVOBuilder {
        private String name;
        private String description;
        private Long operatorId;
        private List<Param> arguments;
        private List<Variable> variableDefs;
        private ScheduleConf scheduleConf;
        private List<TaskDependency> dependencies;
        private List<Tag> tags;

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

        public TaskPropsVOBuilder withArguments(List<Param> arguments) {
            this.arguments = arguments;
            return this;
        }

        public TaskPropsVOBuilder withVariableDefs(List<Variable> variableDefs) {
            this.variableDefs = variableDefs;
            return this;
        }

        public TaskPropsVOBuilder withScheduleConf(ScheduleConf scheduleConf) {
            this.scheduleConf = scheduleConf;
            return this;
        }

        public TaskPropsVOBuilder withDependencies(List<TaskDependency> dependencies) {
            this.dependencies = dependencies;
            return this;
        }

        public TaskPropsVOBuilder withTags(List<Tag> tags) {
            this.tags = tags;
            return this;
        }

        public TaskPropsVO build() {
            return new TaskPropsVO(this);
        }
    }
}
