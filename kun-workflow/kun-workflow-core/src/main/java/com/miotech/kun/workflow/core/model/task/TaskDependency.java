package com.miotech.kun.workflow.core.model.task;

public class TaskDependency {
    private final Long upstreamTaskId;

    private Long downstreamTaskId;

    private final DependencyFunction dependencyFunc;

    public TaskDependency(Long upstreamTaskId, DependencyFunction dependencyFunc) {
        this.upstreamTaskId = upstreamTaskId;
        this.dependencyFunc = dependencyFunc;
        this.downstreamTaskId = null;
    }

    public TaskDependency(Long upstreamTaskId, DependencyFunction dependencyFunc, Long downstreamTaskId) {
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
