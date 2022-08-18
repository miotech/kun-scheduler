package com.miotech.kun.workflow.client.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(builder = TaskRunDependency.Builder.class)
public class TaskRunDependency {

    private final Long downstreamTaskRunId;

    private final Long upstreamTaskRunId;

    public TaskRunDependency(Long downstreamTaskRunId, Long upstreamTaskRunId) {
        this.downstreamTaskRunId = downstreamTaskRunId;
        this.upstreamTaskRunId = upstreamTaskRunId;
    }

    public Long getDownstreamTaskRunId() {
        return downstreamTaskRunId;
    }

    public Long getUpstreamTaskRunId() {
        return upstreamTaskRunId;
    }

    public static Builder newBuilder() { return new Builder(); }

    public static final class Builder {
        private Long downstreamTaskRunId;
        private Long upstreamTaskRunId;

        private Builder() {
        }

        public Builder withDownstreamTaskRunId(Long downstreamTaskRunId) {
            this.downstreamTaskRunId = downstreamTaskRunId;
            return this;
        }

        public Builder withUpstreamTaskRunId(Long upstreamTaskRunId) {
            this.upstreamTaskRunId = upstreamTaskRunId;
            return this;
        }

        public TaskRunDependency build() {
            return new TaskRunDependency(downstreamTaskRunId, upstreamTaskRunId);
        }
    }
}
