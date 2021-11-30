package com.miotech.kun.dataquality.web.model;

import com.miotech.kun.workflow.client.model.TaskRun;

import java.time.OffsetDateTime;

public class AbnormalDataset {

    private final Long id;

    private final Long datasetGid;

    private final Long taskRunId;

    private final Long taskId;

    private final String taskName;

    private final OffsetDateTime createTime;

    private final OffsetDateTime updateTime;

    private final String scheduleAt;

    private final String status;

    public AbnormalDataset(Long id, Long datasetGid, Long taskRunId, Long taskId, String taskName, OffsetDateTime createTime,
                           OffsetDateTime updateTime, String scheduleAt, String status) {
        this.id = id;
        this.datasetGid = datasetGid;
        this.taskRunId = taskRunId;
        this.taskId = taskId;
        this.taskName = taskName;
        this.createTime = createTime;
        this.updateTime = updateTime;
        this.scheduleAt = scheduleAt;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public Long getDatasetGid() {
        return datasetGid;
    }

    public Long getTaskRunId() {
        return taskRunId;
    }

    public Long getTaskId() {
        return taskId;
    }

    public String getTaskName() {
        return taskName;
    }

    public OffsetDateTime getCreateTime() {
        return createTime;
    }

    public OffsetDateTime getUpdateTime() {
        return updateTime;
    }

    public String getScheduleAt() {
        return scheduleAt;
    }

    public String getStatus() {
        return status;
    }

    public static AbnormalDataset from(TaskRun taskRun) {
        return newBuilder()
                .withTaskRunId(taskRun.getId())
                .withTaskId(taskRun.getTask().getId())
                .withTaskName(taskRun.getTask().getName())
                .build();
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public Builder cloneBuilder() {
        return newBuilder()
                .withId(id)
                .withDatasetGid(datasetGid)
                .withTaskRunId(taskRunId)
                .withTaskId(taskId)
                .withTaskName(taskName)
                .withCreateTime(createTime)
                .withUpdateTime(updateTime)
                .withScheduleAt(scheduleAt)
                .withStatus(status)
                ;
    }

    public static final class Builder {
        private Long id;
        private Long datasetGid;
        private Long taskRunId;
        private Long taskId;
        private String taskName;
        private OffsetDateTime createTime;
        private OffsetDateTime updateTime;
        private String scheduleAt;
        private String status;

        private Builder() {
        }

        public Builder withId(Long id) {
            this.id = id;
            return this;
        }

        public Builder withDatasetGid(Long datasetGid) {
            this.datasetGid = datasetGid;
            return this;
        }

        public Builder withTaskRunId(Long taskRunId) {
            this.taskRunId = taskRunId;
            return this;
        }

        public Builder withTaskId(Long taskId) {
            this.taskId = taskId;
            return this;
        }

        public Builder withTaskName(String taskName) {
            this.taskName = taskName;
            return this;
        }

        public Builder withCreateTime(OffsetDateTime createTime) {
            this.createTime = createTime;
            return this;
        }

        public Builder withUpdateTime(OffsetDateTime updateTime) {
            this.updateTime = updateTime;
            return this;
        }

        public Builder withScheduleAt(String scheduleAt) {
            this.scheduleAt = scheduleAt;
            return this;
        }

        public Builder withStatus(String status) {
            this.status = status;
            return this;
        }

        public AbnormalDataset build() {
            return new AbnormalDataset(id, datasetGid, taskRunId, taskId, taskName, createTime, updateTime, scheduleAt, status);
        }
    }
}
