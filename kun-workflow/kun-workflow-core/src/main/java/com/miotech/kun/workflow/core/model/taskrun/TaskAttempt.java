package com.miotech.kun.workflow.core.model.taskrun;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.miotech.kun.workflow.utils.JsonLongFieldDeserializer;

import java.time.OffsetDateTime;

import static com.google.common.base.Preconditions.checkNotNull;

@JsonDeserialize(builder = TaskAttempt.Builder.class)
public class TaskAttempt {
    @JsonSerialize(using = ToStringSerializer.class)
    @JsonDeserialize(using = JsonLongFieldDeserializer.class)
    private final Long id;

    private final TaskRun taskRun;

    private final int attempt;

    private final TaskRunStatus status;

    private final String queueName;

    private final String logPath;

    private final OffsetDateTime startAt;

    private final OffsetDateTime endAt;

    public TaskAttempt(Long id, TaskRun taskRun, int attempt, TaskRunStatus status,
                       String logPath, OffsetDateTime startAt, OffsetDateTime endAt,String queueName) {
        checkNotNull(taskRun, "taskRun should not be null.");
        checkNotNull(taskRun.getTask(), "task should not be null.");
        this.id = id;
        this.taskRun = taskRun;
        this.attempt = attempt;
        this.status = status;
        this.logPath = logPath;
        this.startAt = startAt;
        this.endAt = endAt;
        this.queueName = queueName;
    }

    public Long getId() {
        return id;
    }

    public TaskRun getTaskRun() {
        return taskRun;
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

    public Long getTaskId() {
        return getTaskRun().getTask().getId();
    }

    public String getTaskName() {
        return getTaskRun().getTask().getName();
    }

    public String getQueueName() {
        return queueName;
    }

    public static TaskAttempt.Builder newBuilder() {
        return new TaskAttempt.Builder();
    }

    public TaskAttempt.Builder cloneBuilder() {
        return new Builder()
                .withId(id)
                .withTaskRun(taskRun)
                .withAttempt(attempt)
                .withStatus(status)
                .withLogPath(logPath)
                .withStartAt(startAt)
                .withEndAt(endAt)
                .withQueueName(queueName);
    }

    @Override
    public String toString() {
        return "TaskAttempt{" +
                "id=" + id +
                ", taskRun=" + taskRun +
                ", attempt=" + attempt +
                ", status=" + status +
                ", queueName='" + queueName + '\'' +
                ", logPath='" + logPath + '\'' +
                ", startAt=" + startAt +
                ", endAt=" + endAt +
                '}';
    }

    @JsonPOJOBuilder
    public static final class Builder {
        private Long id;
        private TaskRun taskRun;
        private int attempt;
        private TaskRunStatus status;
        private String logPath;
        private OffsetDateTime startAt;
        private OffsetDateTime endAt;
        private String queueName;

        private Builder() {
        }

        public Builder withId(Long id) {
            this.id = id;
            return this;
        }

        public Builder withTaskRun(TaskRun taskRun) {
            this.taskRun = taskRun;
            return this;
        }

        public Builder withAttempt(int attempt) {
            this.attempt = attempt;
            return this;
        }

        public Builder withStatus(TaskRunStatus status) {
            this.status = status;
            return this;
        }

        public Builder withLogPath(String logPath) {
            this.logPath = logPath;
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

        public Builder withQueueName(String queueName){
            this.queueName = queueName;
            return this;
        }

        public TaskAttempt build() {
            return new TaskAttempt(id, taskRun, attempt, status, logPath, startAt, endAt,queueName);
        }
    }
}
