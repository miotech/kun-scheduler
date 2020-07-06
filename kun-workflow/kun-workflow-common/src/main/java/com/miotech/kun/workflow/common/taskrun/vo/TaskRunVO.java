package com.miotech.kun.workflow.common.taskrun.vo;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.miotech.kun.workflow.core.model.common.Tick;
import com.miotech.kun.workflow.core.model.common.Variable;
import com.miotech.kun.workflow.core.model.lineage.DataStore;
import com.miotech.kun.workflow.core.model.task.Task;
import com.miotech.kun.workflow.core.model.taskrun.TaskAttempt;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunStatus;

import java.time.OffsetDateTime;
import java.util.List;

@JsonDeserialize(builder = TaskRunVO.Builder.class)
public class TaskRunVO {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    private Task task;

    private List<Variable> variables;

    private Tick scheduledTick;

    private TaskRunStatus status;

    private List<DataStore> inlets;

    private List<DataStore> outlets;

    private OffsetDateTime startAt;

    private OffsetDateTime endAt;

    private List<TaskAttempt> attempts;

    @JsonSerialize(contentUsing = ToStringSerializer.class)
    private List<Long> dependentTaskRunIds;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    public List<Variable> getVariables() {
        return variables;
    }

    public void setVariables(List<Variable> variables) {
        this.variables = variables;
    }

    public Tick getScheduledTick() {
        return scheduledTick;
    }

    public void setScheduledTick(Tick scheduledTick) {
        this.scheduledTick = scheduledTick;
    }

    public TaskRunStatus getStatus() {
        return status;
    }

    public void setStatus(TaskRunStatus status) {
        this.status = status;
    }

    public List<DataStore> getInlets() {
        return inlets;
    }

    public void setInlets(List<DataStore> inlets) {
        this.inlets = inlets;
    }

    public List<DataStore> getOutlets() {
        return outlets;
    }

    public void setOutlets(List<DataStore> outlets) {
        this.outlets = outlets;
    }

    public OffsetDateTime getStartAt() {
        return startAt;
    }

    public void setStartAt(OffsetDateTime startAt) {
        this.startAt = startAt;
    }

    public OffsetDateTime getEndAt() {
        return endAt;
    }

    public void setEndAt(OffsetDateTime endAt) {
        this.endAt = endAt;
    }

    public List<TaskAttempt> getAttempts() {
        return attempts;
    }

    public void setAttempts(List<TaskAttempt> attempts) {
        this.attempts = attempts;
    }

    public static TaskRunVO.Builder newBuilder() {
        return new TaskRunVO.Builder();
    }

    public List<Long> getDependentTaskRunIds() {
        return dependentTaskRunIds;
    }

    public void setDependentTaskRunIds(List<Long> dependencyTaskRunIds) {
        this.dependentTaskRunIds = dependencyTaskRunIds;
    }

    @JsonPOJOBuilder
    public static final class Builder {
        private Long id;
        private Task task;
        private List<Variable> variables;
        private Tick scheduledTick;
        private TaskRunStatus status;
        private List<DataStore> inlets;
        private List<DataStore> outlets;
        private OffsetDateTime startAt;
        private OffsetDateTime endAt;
        private List<TaskAttempt> attempts;
        private List<Long> dependentTaskRunIds;

        private Builder() {
        }

        public static Builder aTaskRunVO() {
            return new Builder();
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

        public Builder withInlets(List<DataStore> inlets) {
            this.inlets = inlets;
            return this;
        }

        public Builder withOutlets(List<DataStore> outlets) {
            this.outlets = outlets;
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

        public Builder withAttempts(List<TaskAttempt> attempts) {
            this.attempts = attempts;
            return this;
        }

        public Builder withDependentTaskRunIds(List<Long> dependencyTaskRunIds) {
            this.dependentTaskRunIds = dependencyTaskRunIds;
            return this;
        }

        public TaskRunVO build() {
            TaskRunVO taskRunVO = new TaskRunVO();
            taskRunVO.setId(id);
            taskRunVO.setTask(task);
            taskRunVO.setVariables(variables);
            taskRunVO.setScheduledTick(scheduledTick);
            taskRunVO.setStatus(status);
            taskRunVO.setInlets(inlets);
            taskRunVO.setOutlets(outlets);
            taskRunVO.setStartAt(startAt);
            taskRunVO.setEndAt(endAt);
            taskRunVO.setAttempts(attempts);
            taskRunVO.setDependentTaskRunIds(dependentTaskRunIds);
            return taskRunVO;
        }
    }
}
