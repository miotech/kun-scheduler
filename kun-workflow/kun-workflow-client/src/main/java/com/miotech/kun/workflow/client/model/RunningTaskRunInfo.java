package com.miotech.kun.workflow.client.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.miotech.kun.commons.utils.JsonLongFieldDeserializer;

@JsonDeserialize(builder = RunningTaskRunInfo.Builder.class)
public class RunningTaskRunInfo {

    @JsonDeserialize(using = JsonLongFieldDeserializer.class)
    private Long taskRunId;

    private String name;

    @JsonDeserialize(using = JsonLongFieldDeserializer.class)
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
            runningTaskRunInfo.runningTime_seconds = this.runningTime_seconds;
            runningTaskRunInfo.name = this.name;
            runningTaskRunInfo.taskRunId = this.taskRunId;
            return runningTaskRunInfo;
        }
    }
}
