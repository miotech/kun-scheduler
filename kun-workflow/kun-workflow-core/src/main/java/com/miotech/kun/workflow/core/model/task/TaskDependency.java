package com.miotech.kun.workflow.core.model.task;

public class TaskDependency {
    private final Long upstreamTaskId;

    private final Long downstreamTaskId;

    private final DependencyFunction dependencyFunc;

    public TaskDependency(Long upstreamTaskId, Long downstreamTaskId, DependencyFunction dependencyFunc) {
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

    public DependencyFunction getDependencyFunction() {
        return dependencyFunc;
    }
}
