package com.miotech.kun.workflow.core.model.task;

public class TaskDependency {
    private final Long upstreamTaskId;

    private final DependencyFunction dependencyFunc;

    public TaskDependency(Long upstreamTaskId, DependencyFunction dependencyFunc) {
        this.upstreamTaskId = upstreamTaskId;
        this.dependencyFunc = dependencyFunc;
    }

    public Long getUpstreamTaskId() {
        return upstreamTaskId;
    }

    public DependencyFunction getDependencyFunction() {
        return dependencyFunc;
    }
}
