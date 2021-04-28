package com.miotech.kun.workflow.client.model;

public class TaskRunLogRequest {

    private final long taskRunId;

    private final int attempt;

    private final Integer startLine;

    private final Integer endLine;

    private TaskRunLogRequest(Builder builder) {
        this.taskRunId = builder.taskRunId;
        this.attempt = builder.attempt;
        this.startLine = builder.startLine;
        this.endLine = builder.endLine;
    }

    public long getTaskRunId() {
        return taskRunId;
    }

    public int getAttempt() {
        return attempt;
    }

    public Integer getStartLine() {
        return startLine;
    }

    public Integer getEndLine() {
        return endLine;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static final class Builder {
        private long taskRunId;
        private int attempt;
        private Integer startLine;
        private Integer endLine;

        private Builder() {
        }

        public TaskRunLogRequest build() {
            return new TaskRunLogRequest(this);
        }

        public Builder withTaskRunId(long taskRunId) {
            this.taskRunId = taskRunId;
            return this;
        }

        public Builder withAttempt(int attempt) {
            this.attempt = attempt;
            return this;
        }

        public Builder withStartLine(Integer startLine) {
            this.startLine = startLine;
            return this;
        }

        public Builder withEndLine(Integer endLine) {
            this.endLine = endLine;
            return this;
        }
    }
}
