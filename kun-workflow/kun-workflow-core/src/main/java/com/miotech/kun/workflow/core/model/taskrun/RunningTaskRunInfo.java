package com.miotech.kun.workflow.core.model.taskrun;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

public class RunningTaskRunInfo {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long taskRunId;

    private String name;

    private TaskRunStatus status;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long runningTime_seconds;

    public static Builder newBuilder() {
        return new Builder();
    }

    public Long getTaskRunId() {
        return taskRunId;
    }

    public String getName() {
        return name;
    }

    public TaskRunStatus getStatus() {
        return status;
    }

    public Long getRunningTime_seconds() {
        return runningTime_seconds;
    }

    public void setTaskRunId(Long taskRunId) {
        this.taskRunId = taskRunId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setStatus(TaskRunStatus status) {
        this.status = status;
    }

    public void setRunningTime_seconds(Long runningTime_seconds) {
        this.runningTime_seconds = runningTime_seconds;
    }

    public static final class Builder {
        private Long taskRunId;
        private String name;
        private TaskRunStatus status;
        private Long runningTime_seconds;

        private Builder() {
        }

        public Builder withTaskRunId(Long taskRunId) {
            this.taskRunId = taskRunId;
            return this;
        }

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public Builder withStatus(TaskRunStatus status) {
            this.status = status;
            return this;
        }

        public Builder withRunningTime_seconds(Long runningTime_seconds) {
            this.runningTime_seconds = runningTime_seconds;
            return this;
        }

        public RunningTaskRunInfo build() {
            RunningTaskRunInfo runningTaskRunInfo = new RunningTaskRunInfo();
            runningTaskRunInfo.setTaskRunId(taskRunId);
            runningTaskRunInfo.setName(name);
            runningTaskRunInfo.setStatus(status);
            runningTaskRunInfo.setRunningTime_seconds(runningTime_seconds);
            return runningTaskRunInfo;
        }
    }
}


