package com.miotech.kun.workflow.client.model;

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
}

