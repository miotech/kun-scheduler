package com.miotech.kun.workflow.core.model.taskrun;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

public class RunningTaskRunInfo {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long taskRunId;

    private String name;

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

    public Long getRunningTime_seconds() {
        return runningTime_seconds;
    }

    public static final class Builder {
        private Long taskRunId;
        private String name;
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

        public Builder withRunningTime_seconds(Long runningTime_seconds) {
            this.runningTime_seconds = runningTime_seconds;
            return this;
        }

        public RunningTaskRunInfo build() {
            RunningTaskRunInfo runningTaskRunInfo = new RunningTaskRunInfo();
            runningTaskRunInfo.name = this.name;
            runningTaskRunInfo.taskRunId = this.taskRunId;
            runningTaskRunInfo.runningTime_seconds = this.runningTime_seconds;
            return runningTaskRunInfo;
        }
    }
}
