package com.miotech.kun.workflow.common.task.vo;

import com.miotech.kun.workflow.core.model.common.Param;
import com.miotech.kun.workflow.core.model.common.Variable;
import com.miotech.kun.workflow.core.model.task.ScheduleConf;
import com.miotech.kun.workflow.core.model.task.Task;
import com.miotech.kun.workflow.core.model.task.TaskDependency;

import java.util.List;

public class TaskPropsVO {
    private final String name;
    private final String description;
    private final Long operatorId;
    private final List<Param> arguments;
    private final List<Variable> variableDefs;
    private final ScheduleConf scheduleConf;
    private final List<TaskDependency> dependencies;

    public TaskPropsVO(String name, String description, Long operatorId, List<Param> arguments, List<Variable> variableDefs, ScheduleConf scheduleConf, List<TaskDependency> dependencies) {
        this.name = name;
        this.description = description;
        this.operatorId = operatorId;
        this.arguments = arguments;
        this.variableDefs = variableDefs;
        this.scheduleConf = scheduleConf;
        this.dependencies = dependencies;
    }

    public static TaskPropsVOBuilder newBuilder() {
        return new TaskPropsVOBuilder();
    }

    public static TaskPropsVO from(Task task) {
        return new TaskPropsVO(
                task.getName(),
                task.getDescription(),
                task.getOperatorId(),
                task.getArguments(),
                task.getVariableDefs(),
                task.getScheduleConf(),
                task.getDependencies()
        );
    }

    public TaskPropsVOBuilder cloneBuilder() {
        return new TaskPropsVOBuilder()
                .withName(name)
                .withDescription(description)
                .withOperatorId(operatorId)
                .withArguments(arguments)
                .withVariableDefs(variableDefs)
                .withScheduleConf(scheduleConf)
                .withDependencies(dependencies);
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

    public List<TaskDependency> getDependencies() { return dependencies; }

    public static final class TaskPropsVOBuilder {
        private String name;
        private String description;
        private Long operatorId;
        private List<Param> arguments;
        private List<Variable> variableDefs;
        private ScheduleConf scheduleConf;
        private List<TaskDependency> dependencies;

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

        public TaskPropsVO build() {
            return new TaskPropsVO(name, description, operatorId, arguments, variableDefs, scheduleConf, dependencies);
        }
    }
}
