package com.miotech.kun.workflow.client.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(builder = TaskDependency.Builder.class)
public class TaskDependency {
    private final Long upstreamTaskId;

    private final String dependencyFunc;

    public TaskDependency(Long upstreamTaskId, String dependencyFunc) {
        this.upstreamTaskId = upstreamTaskId;
        this.dependencyFunc = dependencyFunc;
    }

    public Long getUpstreamTaskId() {
        return upstreamTaskId;
    }

    public String getDependencyFunc() {
        return dependencyFunc;
    }

    public static Builder newBuilder() { return new Builder(); }

    public static final class Builder {
        private Long upstreamTaskId;
        private String dependencyFunc;

        private Builder() {
        }

        public static Builder aTaskDependency() {
            return new Builder();
        }

        public Builder withUpstreamTaskId(Long upstreamTaskId) {
            this.upstreamTaskId = upstreamTaskId;
            return this;
        }

        public Builder withDependencyFunc(String dependencyFunc) {
            this.dependencyFunc = dependencyFunc;
            return this;
        }

        public TaskDependency build() {
            return new TaskDependency(upstreamTaskId, dependencyFunc);
        }
    }
}

