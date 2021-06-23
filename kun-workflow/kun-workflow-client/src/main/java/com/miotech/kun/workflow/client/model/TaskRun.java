package com.miotech.kun.workflow.client.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.miotech.kun.commons.utils.CustomDateTimeDeserializer;
import com.miotech.kun.commons.utils.CustomDateTimeSerializer;
import com.miotech.kun.metadata.core.model.dataset.DataStore;
import com.miotech.kun.workflow.core.execution.Config;
import com.miotech.kun.workflow.core.model.common.Tag;
import com.miotech.kun.workflow.core.model.common.Tick;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunStatus;

import java.time.OffsetDateTime;
import java.util.List;

@JsonDeserialize(builder = TaskRun.Builder.class)
public class TaskRun {

    private Long id;

    private Task task;

    private Config config;

    private Tick scheduledTick;

    private TaskRunStatus status;

    private List<DataStore> inlets;

    private List<DataStore> outlets;

    private String scheduleType;

    private String queueName;

    @JsonDeserialize(using = CustomDateTimeDeserializer.class)
    @JsonSerialize(using = CustomDateTimeSerializer.class)
    private OffsetDateTime startAt;

    @JsonDeserialize(using = CustomDateTimeDeserializer.class)
    @JsonSerialize(using = CustomDateTimeSerializer.class)
    private OffsetDateTime endAt;

    @JsonDeserialize(using = CustomDateTimeDeserializer.class)
    @JsonSerialize(using = CustomDateTimeSerializer.class)
    private OffsetDateTime createdAt;

    @JsonDeserialize(using = CustomDateTimeDeserializer.class)
    @JsonSerialize(using = CustomDateTimeSerializer.class)
    private OffsetDateTime updatedAt;

    private List<TaskAttempt> attempts;

    private List<Long> dependencyTaskRunIds;

    private List<Tag> tags;

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

    public Config getConfig() {
        return config;
    }

    public void setConfig(Config config) {
        this.config = config;
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

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public List<TaskAttempt> getAttempts() {
        return attempts;
    }

    public void setAttempts(List<TaskAttempt> attempts) {
        this.attempts = attempts;
    }

    public List<Tag> getTags() { return tags; }

    public void setTags(List<Tag> tags) { this.tags = tags; }

    public static Builder newBuilder() {
        return new Builder();
    }

    public List<Long> getDependencyTaskRunIds() {
        return dependencyTaskRunIds;
    }

    public String getScheduleType() {
        return scheduleType;
    }

    public void setDependencyTaskRunIds(List<Long> dependencyTaskRunIds) {
        this.dependencyTaskRunIds = dependencyTaskRunIds;
    }

    public String getQueueName() {
        return queueName;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    public static final class Builder {
        private Long id;
        private Task task;
        private Config config;
        private Tick scheduledTick;
        private TaskRunStatus status;
        private List<DataStore> inlets;
        private List<DataStore> outlets;
        @JsonDeserialize(using = CustomDateTimeDeserializer.class)
        private OffsetDateTime startAt;
        @JsonDeserialize(using = CustomDateTimeDeserializer.class)
        private OffsetDateTime endAt;
        @JsonDeserialize(using = CustomDateTimeDeserializer.class)
        private OffsetDateTime createdAt;
        @JsonDeserialize(using = CustomDateTimeDeserializer.class)
        private OffsetDateTime updatedAt;
        private List<TaskAttempt> attempts;
        private List<Long> dependencyTaskRunIds;
        private List<Tag> tags;
        private String scheduleType;
        private String queueName;

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

        public Builder withConfig(Config config) {
            this.config = config;
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

        public Builder withCreatedAt(OffsetDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder withUpdatedAt(OffsetDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public Builder withAttempts(List<TaskAttempt> attempts) {
            this.attempts = attempts;
            return this;
        }

        public Builder withDependencyTaskRunIds(List<Long> dependencyTaskRunIds) {
            this.dependencyTaskRunIds = dependencyTaskRunIds;
            return this;
        }

        public Builder withTags(List<Tag> tags) {
            this.tags = tags;
            return this;
        }

        public Builder withScheduleType(String scheduleType) {
            this.scheduleType = scheduleType;
            return this;
        }
        public Builder withQueueName(String queueName){
            this.queueName = queueName;
            return this;
        }

        public TaskRun build() {
            TaskRun taskRunVO = new TaskRun();
            taskRunVO.setId(id);
            taskRunVO.setTask(task);
            taskRunVO.setConfig(config);
            taskRunVO.setScheduledTick(scheduledTick);
            taskRunVO.setStatus(status);
            taskRunVO.setInlets(inlets);
            taskRunVO.setOutlets(outlets);
            taskRunVO.setStartAt(startAt);
            taskRunVO.setEndAt(endAt);
            taskRunVO.setAttempts(attempts);
            taskRunVO.setDependencyTaskRunIds(dependencyTaskRunIds);
            taskRunVO.setTags(tags);
            taskRunVO.setQueueName(queueName);
            taskRunVO.createdAt = createdAt;
            taskRunVO.updatedAt = updatedAt;
            taskRunVO.scheduleType = scheduleType;
            return taskRunVO;
        }
    }
}
