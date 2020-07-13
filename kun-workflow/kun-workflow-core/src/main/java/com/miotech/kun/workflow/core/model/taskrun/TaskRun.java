package com.miotech.kun.workflow.core.model.taskrun;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.miotech.kun.workflow.core.model.common.Tick;
import com.miotech.kun.workflow.core.model.common.Variable;
import com.miotech.kun.workflow.core.model.lineage.DataStore;
import com.miotech.kun.workflow.core.model.task.Task;

import java.time.OffsetDateTime;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

public class TaskRun {
    @JsonSerialize(using = ToStringSerializer.class)
    private final Long id;

    private final Task task;

    private final List<Variable> variables;

    private final Tick scheduledTick;

    private final TaskRunStatus status;

    private final OffsetDateTime startAt;

    private final OffsetDateTime endAt;

    private final List<DataStore> inlets;

    private final List<DataStore> outlets;

    @JsonSerialize(contentUsing = ToStringSerializer.class)
    private final List<Long> dependentTaskRunIds;

    public Long getId() {
        return id;
    }

    public Task getTask() {
        return task;
    }

    public List<Variable> getVariables() {
        return variables;
    }

    public Tick getScheduledTick() {
        return scheduledTick;
    }

    public TaskRunStatus getStatus() {
        return status;
    }

    public OffsetDateTime getStartAt() {
        return startAt;
    }

    public OffsetDateTime getEndAt() {
        return endAt;
    }

    public List<DataStore> getInlets() {
        return inlets;
    }

    public List<DataStore> getOutlets() {
        return outlets;
    }

    public List<Long> getDependentTaskRunIds() {
        return dependentTaskRunIds;
    }

    public TaskRun(Long id, Task task, List<Variable> variables, Tick scheduledTick, TaskRunStatus status,
                   OffsetDateTime startAt, OffsetDateTime endAt, List<DataStore> inlets, List<DataStore> outlets,
                   List<Long> dependentTaskRunIds) {
        checkNotNull(task, "task should not be null.");
        this.id = id;
        this.task = task;
        this.variables = variables;
        this.scheduledTick = scheduledTick;
        this.status = status;
        this.startAt = startAt;
        this.endAt = endAt;
        this.inlets = inlets;
        this.outlets = outlets;
        this.dependentTaskRunIds = dependentTaskRunIds;
    }

    public static TaskRunBuilder newBuilder() {
        return new TaskRunBuilder();
    }

    public TaskRunBuilder cloneBuilder() {
        return newBuilder()
                .withId(id)
                .withTask(task)
                .withVariables(variables)
                .withScheduledTick(scheduledTick)
                .withStatus(status)
                .withStartAt(startAt)
                .withEndAt(endAt)
                .withInlets(inlets)
                .withOutlets(outlets)
                .withDependentTaskRunIds(dependentTaskRunIds);
    }

    public static final class TaskRunBuilder {
        private Long id;
        private Task task;
        private List<Variable> variables;
        private Tick scheduledTick;
        private TaskRunStatus status;
        private OffsetDateTime startAt;
        private OffsetDateTime endAt;
        private List<DataStore> inlets;
        private List<DataStore> outlets;
        private List<Long> dependentTaskRunIds;

        private TaskRunBuilder() {
        }

        public TaskRunBuilder withId(Long id) {
            this.id = id;
            return this;
        }

        public TaskRunBuilder withTask(Task task) {
            this.task = task;
            return this;
        }

        public TaskRunBuilder withVariables(List<Variable> variables) {
            this.variables = variables;
            return this;
        }

        public TaskRunBuilder withScheduledTick(Tick scheduledTick) {
            this.scheduledTick = scheduledTick;
            return this;
        }

        public TaskRunBuilder withStatus(TaskRunStatus status) {
            this.status = status;
            return this;
        }

        public TaskRunBuilder withStartAt(OffsetDateTime startAt) {
            this.startAt = startAt;
            return this;
        }

        public TaskRunBuilder withEndAt(OffsetDateTime endAt) {
            this.endAt = endAt;
            return this;
        }

        public TaskRunBuilder withInlets(List<DataStore> inlets) {
            this.inlets = inlets;
            return this;
        }

        public TaskRunBuilder withOutlets(List<DataStore> outlets) {
            this.outlets = outlets;
            return this;
        }

        public TaskRunBuilder withDependentTaskRunIds(List<Long> dependentTaskRunIds) {
            this.dependentTaskRunIds = dependentTaskRunIds;
            return this;
        }

        public TaskRun build() {
            return new TaskRun(id, task, variables, scheduledTick, status, startAt, endAt, inlets, outlets, dependentTaskRunIds);
        }
    }
}
