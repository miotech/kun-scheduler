package com.miotech.kun.workflow.client.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(builder = TaskDependency.Builder.class)
public class TaskDependency {
    private final Long upstreamTaskId;
    private final Long downstreamTaskId;
    private final String dependencyFunc;

    public TaskDependency(Long upstreamTaskId, String dependencyFunc) {
        this(upstreamTaskId, null, dependencyFunc);
    }

    public TaskDependency(Long upstreamTaskId, Long downstreamTaskId, String dependencyFunc) {
        this.upstreamTaskId = upstreamTaskId;
        this.downstreamTaskId = downstreamTaskId;
        this.dependencyFunc = dependencyFunc;
    }

    public Long getUpstreamTaskId() {
        return upstreamTaskId;
    }

    public Long getDownstreamTaskId() {
        return downstreamTaskId;
    }

    public String getDependencyFunc() {
        return dependencyFunc;
    }

    public static Builder newBuilder() { return new Builder(); }

    public static final class Builder {
        private Long upstreamTaskId;
        private Long downstreamTaskId;
        private String dependencyFunc;

        private Builder() {
        }

        public Builder withUpstreamTaskId(Long upstreamTaskId) {
            this.upstreamTaskId = upstreamTaskId;
            return this;
        }

        public Builder withDownstreamTaskId(Long downstreamTaskId) {
            this.downstreamTaskId = downstreamTaskId;
            return this;
        }

        public Builder withDependencyFunc(String dependencyFunc) {
            this.dependencyFunc = dependencyFunc;
            return this;
        }

        public TaskDependency build() {
            return new TaskDependency(upstreamTaskId, downstreamTaskId, dependencyFunc);
        }
    }
}

