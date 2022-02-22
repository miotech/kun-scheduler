package com.miotech.kun.workflow.core.model.common;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.miotech.kun.commons.utils.CustomDateTimeDeserializer;
import com.miotech.kun.commons.utils.CustomDateTimeSerializer;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunStatus;

import java.time.OffsetDateTime;
import java.util.List;

public class GanttChartTaskRunInfo {

    private Long taskRunId;

    private Long taskId;

    private String name;

    private TaskRunStatus status;

    @JsonDeserialize(using = CustomDateTimeDeserializer.class)
    @JsonSerialize(using = CustomDateTimeSerializer.class)
    private OffsetDateTime queuedAt;

    @JsonDeserialize(using = CustomDateTimeDeserializer.class)
    @JsonSerialize(using = CustomDateTimeSerializer.class)
    private OffsetDateTime startAt;

    @JsonDeserialize(using = CustomDateTimeDeserializer.class)
    @JsonSerialize(using = CustomDateTimeSerializer.class)
    private OffsetDateTime endAt;

    @JsonDeserialize(using = CustomDateTimeDeserializer.class)
    @JsonSerialize(using = CustomDateTimeSerializer.class)
    private OffsetDateTime createdAt;

    private Long averageRunningTime;

    private Long averageQueuingTime;

    private List<Long> dependentTaskRunIds;

    public Long getTaskRunId() {
        return taskRunId;
    }

    public void setTaskRunId(Long taskRunId) {
        this.taskRunId = taskRunId;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public TaskRunStatus getStatus() {
        return status;
    }

    public void setStatus(TaskRunStatus status) {
        this.status = status;
    }

    public OffsetDateTime getQueuedAt() {
        return queuedAt;
    }

    public void setQueuedAt(OffsetDateTime queuedAt) {
        this.queuedAt = queuedAt;
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

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Long getAverageRunningTime() {
        return averageRunningTime;
    }

    public void setAverageRunningTime(Long averageRunningTime) {
        this.averageRunningTime = averageRunningTime;
    }

    public Long getAverageQueuingTime() {
        return averageQueuingTime;
    }

    public void setAverageQueuingTime(Long averageQueuingTime) {
        this.averageQueuingTime = averageQueuingTime;
    }

    public List<Long> getDependentTaskRunIds() {
        return dependentTaskRunIds;
    }

    public void setDependentTaskRunIds(List<Long> dependentTaskRunIds) {
        this.dependentTaskRunIds = dependentTaskRunIds;
    }

    public static GanttChartTaskRunInfoBuilder newBuilder() {
        return new GanttChartTaskRunInfoBuilder();
    }

    public static final class GanttChartTaskRunInfoBuilder {
        private Long taskRunId;
        private Long taskId;
        private String name;
        private TaskRunStatus status;
        private OffsetDateTime queuedAt;
        private OffsetDateTime startAt;
        private OffsetDateTime endAt;
        private OffsetDateTime createdAt;
        private Long averageRunningTime;
        private Long averageQueuingTime;
        private List<Long> dependentTaskRunIds;

        private GanttChartTaskRunInfoBuilder() {
        }

        public static GanttChartTaskRunInfoBuilder aGanttChartTaskRunInfo() {
            return new GanttChartTaskRunInfoBuilder();
        }

        public GanttChartTaskRunInfoBuilder withTaskRunId(Long taskRunId) {
            this.taskRunId = taskRunId;
            return this;
        }

        public GanttChartTaskRunInfoBuilder withTaskId(Long taskId) {
            this.taskId = taskId;
            return this;
        }

        public GanttChartTaskRunInfoBuilder withName(String name) {
            this.name = name;
            return this;
        }

        public GanttChartTaskRunInfoBuilder withStatus(TaskRunStatus status) {
            this.status = status;
            return this;
        }

        public GanttChartTaskRunInfoBuilder withQueuedAt(OffsetDateTime queuedAt) {
            this.queuedAt = queuedAt;
            return this;
        }

        public GanttChartTaskRunInfoBuilder withStartAt(OffsetDateTime startAt) {
            this.startAt = startAt;
            return this;
        }

        public GanttChartTaskRunInfoBuilder withEndAt(OffsetDateTime endAt) {
            this.endAt = endAt;
            return this;
        }

        public GanttChartTaskRunInfoBuilder withCreatedAt(OffsetDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public GanttChartTaskRunInfoBuilder withAverageRunningTime(Long averageRunningTime) {
            this.averageRunningTime = averageRunningTime;
            return this;
        }

        public GanttChartTaskRunInfoBuilder withAverageQueuingTime(Long averageQueuingTime) {
            this.averageQueuingTime = averageQueuingTime;
            return this;
        }

        public GanttChartTaskRunInfoBuilder withDependentTaskRunIds(List<Long> dependentTaskRunIds) {
            this.dependentTaskRunIds = dependentTaskRunIds;
            return this;
        }

        public GanttChartTaskRunInfo build() {
            GanttChartTaskRunInfo ganttChartTaskRunInfo = new GanttChartTaskRunInfo();
            ganttChartTaskRunInfo.setTaskRunId(taskRunId);
            ganttChartTaskRunInfo.setTaskId(taskId);
            ganttChartTaskRunInfo.setName(name);
            ganttChartTaskRunInfo.setStatus(status);
            ganttChartTaskRunInfo.setQueuedAt(queuedAt);
            ganttChartTaskRunInfo.setStartAt(startAt);
            ganttChartTaskRunInfo.setEndAt(endAt);
            ganttChartTaskRunInfo.setCreatedAt(createdAt);
            ganttChartTaskRunInfo.setAverageRunningTime(averageRunningTime);
            ganttChartTaskRunInfo.setAverageQueuingTime(averageQueuingTime);
            ganttChartTaskRunInfo.setDependentTaskRunIds(dependentTaskRunIds);
            return ganttChartTaskRunInfo;
        }
    }
}
