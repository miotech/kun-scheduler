package com.miotech.kun.workflow.client.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(builder = TaskRunDependency.Builder.class)
public class TaskRunDependency {

    private final Long downStreamTaskRunId;

    private final Long upstreamTaskRunId;

    public TaskRunDependency(Long downStreamTaskRunId, Long upstreamTaskRunId) {
        this.downStreamTaskRunId = downStreamTaskRunId;
        this.upstreamTaskRunId = upstreamTaskRunId;
    }

    public Long getDownStreamTaskRunId() {
        return downStreamTaskRunId;
    }

    public Long getUpstreamTaskRunId() {
        return upstreamTaskRunId;
    }

    public static Builder newBuilder() { return new Builder(); }

    public static final class Builder {
        private Long downStreamTaskRunId;
        private Long upstreamTaskRunId;

        private Builder() {
        }

        public Builder withDownStreamTaskRunId(Long downStreamTaskRunId) {
            this.downStreamTaskRunId = downStreamTaskRunId;
            return this;
        }

        public Builder withUpstreamTaskRunId(Long upstreamTaskRunId) {
            this.upstreamTaskRunId = upstreamTaskRunId;
            return this;
        }

        public TaskRunDependency build() {
            return new TaskRunDependency(downStreamTaskRunId, upstreamTaskRunId);
        }
    }
}
