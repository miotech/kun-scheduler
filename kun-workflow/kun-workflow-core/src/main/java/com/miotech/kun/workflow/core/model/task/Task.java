package com.miotech.kun.workflow.core.model.task;

import com.google.common.collect.ImmutableList;

import java.util.List;

public class Task {
    private final String id;

    private final String name;

    private final String description;

    private final String operatorName;

    private final List<Param> params;

    private final List<Variable> variables;

    private final ScheduleConf scheduleConf;

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getOperatorName() {
        return operatorName;
    }

    public List<Param> getParams() {
        return params;
    }

    public List<Variable> getVariables() {
        return variables;
    }

    public ScheduleConf getScheduleConf() {
        return scheduleConf;
    }

    public Task withId(String id) {
        Task.Builder builder = cloneBuilder();
        builder.id(id);
        return builder.build();
    }

    public Task withName(String name) {
        Task.Builder builder = cloneBuilder();
        builder.name(name);
        return builder.build();
    }

    public Task withDescription(String description) {
        Task.Builder builder = cloneBuilder();
        builder.description(description);
        return builder.build();
    }

    public Task withOperatorName(String operatorName) {
        Task.Builder builder = cloneBuilder();
        builder.operatorName(operatorName);
        return builder.build();
    }

    public Task withParams(List<Param> params) {
        Task.Builder builder = cloneBuilder();
        builder.params(params);
        return builder.build();
    }

    public Task withVariables(List<Variable> variables) {
        Task.Builder builder = cloneBuilder();
        builder.variables(variables);
        return builder.build();
    }

    public Task withScheduleConf(ScheduleConf scheduleConf) {
        Task.Builder builder = cloneBuilder();
        builder.scheduleConf(scheduleConf);
        return builder.build();
    }

    private Task(String id, String name, String description, String operatorName, List<Param> params, List<Variable> variables, ScheduleConf scheduleConf) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.operatorName = operatorName;
        this.params = ImmutableList.copyOf(params);
        this.variables = ImmutableList.copyOf(variables);
        this.scheduleConf = scheduleConf;
    }

    public Task.Builder cloneBuilder() {
        return newBuilder()
                .id(id)
                .name(name)
                .description(description)
                .operatorName(operatorName)
                .params(params)
                .variables(variables)
                .scheduleConf(scheduleConf);
    }

    public static Task.Builder newBuilder() {
        return new Task.Builder();
    }

    public static final class Builder {
        private String id;
        private String name;
        private String description;
        private String operatorName;
        private List<Param> params;
        private List<Variable> variables;
        private ScheduleConf scheduleConf;

        private Builder() {
        }

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder operatorName(String operatorName) {
            this.operatorName = operatorName;
            return this;
        }

        public Builder params(List<Param> params) {
            this.params = ImmutableList.copyOf(params);
            return this;
        }

        public Builder variables(List<Variable> variables) {
            this.variables = ImmutableList.copyOf(variables);
            return this;
        }

        public Builder scheduleConf(ScheduleConf scheduleConf) {
            this.scheduleConf = scheduleConf;
            return this;
        }

        public Task build() {
            return new Task(id, name, description, operatorName, params, variables, scheduleConf);
        }
    }
}
