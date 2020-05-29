package com.miotech.kun.workflow.core.model.task;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.collect.ImmutableList;
import com.miotech.kun.workflow.core.model.common.Param;
import com.miotech.kun.workflow.core.model.common.Variable;

import java.util.List;

@JsonDeserialize(builder = Task.TaskBuilder.class)
public class Task {

    private final Long id;

    private final String name;

    private final String description;

    private final Long operatorId;

    private final List<Param> arguments;

    private final List<Variable> variableDefs;

    private final ScheduleConf scheduleConf;

    private final List<TaskDependency> dependencies;

    public Long getId() {
        return id;
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

    public List<TaskDependency> getDependencies() {
        return dependencies;
    }

    private Task(Long id, String name, String description, Long operatorId, List<Param> arguments, List<Variable> variableDefs, ScheduleConf scheduleConf, List<TaskDependency> dependencies) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.operatorId = operatorId;
        this.arguments = ImmutableList.copyOf(arguments);
        this.variableDefs = ImmutableList.copyOf(variableDefs);
        this.scheduleConf = scheduleConf;
        this.dependencies = ImmutableList.copyOf(dependencies);
    }

    public TaskBuilder cloneBuilder() {
        return newBuilder()
                .withId(id)
                .withName(name)
                .withDescription(description)
                .withOperatorId(operatorId)
                .withArguments(arguments)
                .withVariableDefs(variableDefs)
                .withScheduleConf(scheduleConf)
                .withDependencies(dependencies);
    }

    public static TaskBuilder newBuilder() {
        return new TaskBuilder();
    }

    public static final class TaskBuilder {
        private Long id;
        private String name;
        private String description;
        private Long operatorId;
        private List<Param> arguments;
        private List<Variable> variableDefs;
        private ScheduleConf scheduleConf;
        private List<TaskDependency> dependencies;

        private TaskBuilder() {
        }

        public TaskBuilder withId(Long id) {
            this.id = id;
            return this;
        }

        public TaskBuilder withName(String name) {
            this.name = name;
            return this;
        }

        public TaskBuilder withDescription(String description) {
            this.description = description;
            return this;
        }

        public TaskBuilder withOperatorId(Long operatorId) {
            this.operatorId = operatorId;
            return this;
        }

        public TaskBuilder withArguments(List<Param> arguments) {
            this.arguments = arguments;
            return this;
        }

        public TaskBuilder withVariableDefs(List<Variable> variableDefs) {
            this.variableDefs = variableDefs;
            return this;
        }

        public TaskBuilder withScheduleConf(ScheduleConf scheduleConf) {
            this.scheduleConf = scheduleConf;
            return this;
        }

        public TaskBuilder withDependencies(List<TaskDependency> dependencies) {
            this.dependencies = dependencies;
            return this;
        }

        public Task build() {
            return new Task(id, name, description, operatorId, arguments, variableDefs, scheduleConf, dependencies);
        }
    }
}
