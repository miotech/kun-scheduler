package com.miotech.kun.workflow.common.taskrun.bo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunStatus;

import java.time.OffsetDateTime;

/**
 * 和TaskAttempt不同，这个对象只是Value Object，对应的是数据库里的数据。
 * </br>
 * 字段并不完全，目前选取的是最常用的字段。可随业务需求发展添加新的字段。
 */
public class TaskAttemptProps {
    @JsonSerialize(using = ToStringSerializer.class)
    private final Long id;

    @JsonSerialize(using = ToStringSerializer.class)
    private final Long taskRunId;

    @JsonSerialize(using = ToStringSerializer.class)
    private final Long taskId;

    private final String taskName;

    private final int attempt;

    private final TaskRunStatus status;

    private final String logPath;

    private final OffsetDateTime startAt;

    private final OffsetDateTime endAt;

    public Long getId() {
        return id;
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

    public int getAttempt() {
        return attempt;
    }

    public TaskRunStatus getStatus() {
        return status;
    }

    public String getLogPath() {
        return logPath;
    }

    public OffsetDateTime getStartAt() {
        return startAt;
    }

    public OffsetDateTime getEndAt() {
        return endAt;
    }

    public TaskAttemptProps(Long id, Long taskRunId, Long taskId, String taskName, int attempt, TaskRunStatus status,
                            String logPath, OffsetDateTime startAt, OffsetDateTime endAt) {
        this.id = id;
        this.taskRunId = taskRunId;
        this.taskId = taskId;
        this.taskName = taskName;
        this.attempt = attempt;
        this.status = status;
        this.logPath = logPath;
        this.startAt = startAt;
        this.endAt = endAt;
    }

    public static TaskAttemptInfoBuilder newBuilder() {
        return new TaskAttemptInfoBuilder();
    }

    public TaskAttemptInfoBuilder cloneBuilder() {
        return new TaskAttemptInfoBuilder()
                .withId(this.id)
                .withTaskRunId(this.taskRunId)
                .withTaskId(this.taskId)
                .withTaskName(this.taskName)
                .withAttempt(this.attempt)
                .withStatus(this.status)
                .withLogPath(this.logPath)
                .withStartAt(this.startAt)
                .withEndAt(this.endAt);
    }

    public static final class TaskAttemptInfoBuilder {
        private Long id;
        private Long taskRunId;
        private Long taskId;
        private String taskName;
        private int attempt;
        private TaskRunStatus status;
        private String logPath;
        private OffsetDateTime startAt;
        private OffsetDateTime endAt;

        private TaskAttemptInfoBuilder() {
        }

        public TaskAttemptInfoBuilder withId(Long id) {
            this.id = id;
            return this;
        }

        public TaskAttemptInfoBuilder withTaskRunId(Long taskRunId) {
            this.taskRunId = taskRunId;
            return this;
        }

        public TaskAttemptInfoBuilder withTaskId(Long taskId) {
            this.taskId = taskId;
            return this;
        }

        public TaskAttemptInfoBuilder withTaskName(String taskName) {
            this.taskName = taskName;
            return this;
        }

        public TaskAttemptInfoBuilder withAttempt(int attempt) {
            this.attempt = attempt;
            return this;
        }

        public TaskAttemptInfoBuilder withStatus(TaskRunStatus status) {
            this.status = status;
            return this;
        }

        public TaskAttemptInfoBuilder withLogPath(String logPath) {
            this.logPath = logPath;
            return this;
        }

        public TaskAttemptInfoBuilder withStartAt(OffsetDateTime startAt) {
            this.startAt = startAt;
            return this;
        }

        public TaskAttemptInfoBuilder withEndAt(OffsetDateTime endAt) {
            this.endAt = endAt;
            return this;
        }

        public TaskAttemptProps build() {
            return new TaskAttemptProps(id, taskRunId, taskId, taskName, attempt, status, logPath, startAt, endAt);
        }
    }
}
