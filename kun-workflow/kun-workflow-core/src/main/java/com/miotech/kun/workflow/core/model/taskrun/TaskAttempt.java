package com.miotech.kun.workflow.core.model.taskrun;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.miotech.kun.commons.utils.JsonLongFieldDeserializer;

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

    private final Integer priority;

    private final String logPath;

    private final OffsetDateTime startAt;

    private final OffsetDateTime endAt;

    private final Integer retryTimes;

    private final String executorLabel;

    private final String runtimeLabel;

    private final Integer taskRunPhase;

    public TaskAttempt(Long id, TaskRun taskRun, int attempt, TaskRunStatus status,
                       String logPath, OffsetDateTime startAt, OffsetDateTime endAt, String queueName, Integer priority,
                       Integer retryTimes, String executorLabel, String runtimeLabel, Integer taskRunPhase) {
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
        this.priority = priority;
        this.retryTimes = retryTimes;
        this.executorLabel = executorLabel;
        this.runtimeLabel = runtimeLabel;
        this.taskRunPhase = taskRunPhase;
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

    public Integer getPriority() {
        return priority;
    }


    public Integer getRetryTimes() {
        return retryTimes;
    }

    public String getExecutorLabel() {
        return executorLabel;
    }

    public Integer getTaskRunPhase() {
        return taskRunPhase;
    }

    public String getRuntimeLabel() {
        return runtimeLabel;
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
                .withQueueName(queueName)
                .withPriority(priority)
                .withRetryTimes(retryTimes)
                .withExecutorLabel(executorLabel)
                .withPhase(taskRunPhase);
    }

    @Override
    public String toString() {
        return "TaskAttempt{" +
                "id=" + id +
                ", taskRun=" + taskRun +
                ", attempt=" + attempt +
                ", status=" + status +
                ", queueName='" + queueName + '\'' +
                ", priority=" + priority +
                ", logPath='" + logPath + '\'' +
                ", startAt=" + startAt +
                ", endAt=" + endAt +
                ", retryTimes=" + retryTimes +
                ", executorLabel='" + executorLabel + '\'' +
                ", runtimeLabel='" + runtimeLabel + '\'' +
                ", taskRunPhase=" + taskRunPhase +
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
        private Integer priority;
        private Integer retryTimes;
        private String executorLabel;
        private String runtimeLabel;
        private Integer taskRunPhase;

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

        public Builder withQueueName(String queueName) {
            this.queueName = queueName;
            return this;
        }

        public Builder withPriority(Integer priority) {
            this.priority = priority;
            return this;
        }

        public Builder withRetryTimes(Integer retryTimes) {
            this.retryTimes = retryTimes;
            return this;
        }

        public Builder withExecutorLabel(String executorLabel) {
            this.executorLabel = executorLabel;
            return this;
        }

        public Builder withRuntimeLabel(String runtimeLabel) {
            this.runtimeLabel = runtimeLabel;
            return this;
        }

        public Builder withPhase(Integer taskRunPhase) {
            this.taskRunPhase = taskRunPhase;
            return this;
        }

        public TaskAttempt build() {
            return new TaskAttempt(id, taskRun, attempt, status, logPath, startAt, endAt, queueName, priority, retryTimes, executorLabel, runtimeLabel, taskRunPhase);
        }
    }
}
