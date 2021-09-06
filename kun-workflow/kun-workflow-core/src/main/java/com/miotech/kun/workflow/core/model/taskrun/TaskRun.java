package com.miotech.kun.workflow.core.model.taskrun;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.miotech.kun.metadata.core.model.dataset.DataStore;
import com.miotech.kun.workflow.core.execution.Config;
import com.miotech.kun.workflow.core.model.common.Tick;
import com.miotech.kun.workflow.core.model.task.ScheduleType;
import com.miotech.kun.workflow.core.model.task.Task;
import com.miotech.kun.workflow.utils.JsonLongFieldDeserializer;

import java.time.OffsetDateTime;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

public class TaskRun {
    @JsonSerialize(using = ToStringSerializer.class)
    @JsonDeserialize(using = JsonLongFieldDeserializer.class)
    private final Long id;

    private final Task task;

    private final Config config;

    private final Tick scheduledTick;

    private final ScheduleType scheduledType;

    private final TaskRunStatus status;

    private final OffsetDateTime startAt;

    private final OffsetDateTime endAt;

    private final OffsetDateTime createdAt;

    private final OffsetDateTime updatedAt;

    private final List<DataStore> inlets;

    private final List<DataStore> outlets;

    private final Integer priority;

    @JsonSerialize(using = ToStringSerializer.class)
    @JsonDeserialize(using = JsonLongFieldDeserializer.class)
    private final List<Long> dependentTaskRunIds;

    private final String queueName;

    public Long getId() {
        return id;
    }

    public Task getTask() {
        return task;
    }

    public Config getConfig() {
        return config;
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

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
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

    public ScheduleType getScheduledType() {
        return scheduledType;
    }

    public Integer getPriority() {
        return priority;
    }

    public String getQueueName() {
        return queueName;
    }

    public TaskRun(Long id, Task task, Config config, Tick scheduledTick, TaskRunStatus status,
                   OffsetDateTime startAt, OffsetDateTime endAt, OffsetDateTime createdAt, OffsetDateTime updatedAt,
                   List<DataStore> inlets, List<DataStore> outlets, List<Long> dependentTaskRunIds, ScheduleType scheduledType, String queueName, Integer priority) {
        checkNotNull(task, "task should not be null.");
        this.id = id;
        this.task = task;
        this.config = config;
        this.scheduledTick = scheduledTick;
        this.status = status;
        this.startAt = startAt;
        this.endAt = endAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.inlets = inlets;
        this.outlets = outlets;
        this.dependentTaskRunIds = dependentTaskRunIds;
        this.scheduledType = scheduledType;
        this.queueName = queueName;
        this.priority = priority;
    }

    public static TaskRunBuilder newBuilder() {
        return new TaskRunBuilder();
    }

    public TaskRunBuilder cloneBuilder() {
        return newBuilder()
                .withId(id)
                .withTask(task)
                .withConfig(config)
                .withScheduledTick(scheduledTick)
                .withStatus(status)
                .withStartAt(startAt)
                .withEndAt(endAt)
                .withInlets(inlets)
                .withOutlets(outlets)
                .withCreatedAt(createdAt)
                .withUpdatedAt(updatedAt)
                .withDependentTaskRunIds(dependentTaskRunIds)
                .withScheduleType(scheduledType)
                .withQueueName(queueName)
                .withPriority(priority);
    }

    @Override
    public String toString() {
        return "TaskRun{" +
                "id=" + id +
                ", task=" + task +
                ", config=" + config +
                ", scheduledTick=" + scheduledTick +
                ", scheduledType=" + scheduledType +
                ", status=" + status +
                ", startAt=" + startAt +
                ", endAt=" + endAt +
                ", inlets=" + inlets +
                ", outlets=" + outlets +
                ", priority=" + priority +
                ", dependentTaskRunIds=" + dependentTaskRunIds +
                ", queueName='" + queueName + '\'' +
                '}';
    }

    public static final class TaskRunBuilder {
        private Long id;
        private Task task;
        private Config config;
        private Tick scheduledTick;
        private TaskRunStatus status;
        private OffsetDateTime startAt;
        private OffsetDateTime endAt;
        private OffsetDateTime createdAt;
        private OffsetDateTime updatedAt;
        private List<DataStore> inlets;
        private List<DataStore> outlets;
        private List<Long> dependentTaskRunIds;
        private ScheduleType scheduleType;
        private String queueName;
        private Integer priority;

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

        public TaskRunBuilder withConfig(Config config) {
            this.config = config;
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

        public TaskRunBuilder withCreatedAt(OffsetDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public TaskRunBuilder withUpdatedAt(OffsetDateTime updatedAt) {
            this.updatedAt = updatedAt;
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

        public TaskRunBuilder withScheduleType(ScheduleType scheduleType) {
            this.scheduleType = scheduleType;
            return this;
        }
        public TaskRunBuilder withQueueName(String queueName){
            this.queueName = queueName;
            return this;
        }

        public TaskRunBuilder withPriority(Integer priority){
            this.priority = priority;
            return this;
        }

        public TaskRun build() {
            return new TaskRun(id, task, config, scheduledTick, status, startAt, endAt, createdAt, updatedAt, inlets, outlets, dependentTaskRunIds, scheduleType,queueName,priority);
        }
    }
}
