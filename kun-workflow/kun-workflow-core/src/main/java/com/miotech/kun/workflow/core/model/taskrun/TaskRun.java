package com.miotech.kun.workflow.core.model.taskrun;

import com.google.common.collect.ImmutableList;
import com.miotech.kun.workflow.core.model.common.Tick;
import com.miotech.kun.workflow.core.model.entity.Entity;
import com.miotech.kun.workflow.core.model.task.Task;
import com.miotech.kun.workflow.core.model.task.Variable;

import java.time.OffsetDateTime;
import java.util.List;

public class TaskRun {
    private final Long id;

    private final Task task;

    private final List<Variable> variables;

    private final Tick scheduledTick;

    private final TaskRunStatus status;

    private final OffsetDateTime startAt;

    private final OffsetDateTime endAt;

    private final List<Entity> inlets;

    private final List<Entity> outlets;

    private final List<Long> dependencies;

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

    public List<Entity> getInlets() {
        return inlets;
    }

    public List<Entity> getOutlets() {
        return outlets;
    }

    public List<Long> getDependencies() {
        return dependencies;
    }

    public TaskRun(Long id, Task task, List<Variable> variables, Tick scheduledTick, TaskRunStatus status,
                   OffsetDateTime startAt, OffsetDateTime endAt, List<Entity> inlets, List<Entity> outlets, List<Long> dependencies) {
        this.id = id;
        this.task = task;
        this.variables = variables;
        this.scheduledTick = scheduledTick;
        this.status = status;
        this.startAt = startAt;
        this.endAt = endAt;
        this.inlets = ImmutableList.copyOf(inlets);
        this.outlets = ImmutableList.copyOf(outlets);
        this.dependencies = ImmutableList.copyOf(dependencies);
    }

    public static TaskRun.Builder newBuilder() {
        return new TaskRun.Builder();
    }

    public TaskRun.Builder cloneBuilder() {
        return new Builder()
                .withId(id)
                .withTask(task)
                .withVariables(variables)
                .withScheduledTick(scheduledTick)
                .withStatus(status)
                .withStartAt(startAt)
                .withEndAt(endAt)
                .withInlets(inlets)
                .withOutlets(outlets)
                .withDependencies(dependencies);
    }

    public static final class Builder {
        private Long id;
        private Task task;
        private List<Variable> variables;
        private Tick scheduledTick;
        private TaskRunStatus status;
        private OffsetDateTime startAt;
        private OffsetDateTime endAt;
        private List<Entity> inlets;
        private List<Entity> outlets;
        private List<Long> dependencies;

        private Builder() {
        }

        public Builder withId(Long id) {
            this.id = id;
            return this;
        }

        public Builder withTask(Task task) {
            this.task = task;
            return this;
        }

        public Builder withVariables(List<Variable> variables) {
            this.variables = variables;
            return this;
        }

        public Builder withScheduledTick(Tick scheduledTick) {
            this.scheduledTick = scheduledTick;
            return this;
        }

        public Builder withStatus(TaskRunStatus status) {
            this.status = status;
            return this;
        }

        public Builder withStartAt(OffsetDateTime startAt) {
            this.startAt = startAt;
            return this;
        }

        public Builder withEndAt(OffsetDateTime endAt) {
            this.endAt = endAt;
            return this;
        }

        public Builder withInlets(List<Entity> inlets) {
            this.inlets = inlets;
            return this;
        }

        public Builder withOutlets(List<Entity> outlets) {
            this.outlets = outlets;
            return this;
        }

        public Builder withDependencies(List<Long> dependencies) {
            this.dependencies = dependencies;
            return this;
        }

        public TaskRun build() {
            return new TaskRun(id, task, variables, scheduledTick, status, startAt, endAt, inlets, outlets, dependencies);
        }
    }
}
