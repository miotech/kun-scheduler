package com.miotech.kun.workflow.core.model.taskrun;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.miotech.kun.workflow.core.model.task.DependencyLevel;
import com.miotech.kun.workflow.core.model.task.DependencyStatus;

@JsonDeserialize
public class TaskRunDependency {

    private final Long upstreamTaskRunId;

    private final Long downStreamTaskRunId;

    private final DependencyLevel dependencyLevel;

    private final DependencyStatus dependencyStatus;


    @JsonCreator
    public TaskRunDependency(Long upstreamTaskRunId, Long downStreamTaskRunId,
                             DependencyLevel dependencyLevel, DependencyStatus dependencyStatus) {
        this.upstreamTaskRunId = upstreamTaskRunId;
        this.downStreamTaskRunId = downStreamTaskRunId;
        this.dependencyLevel = dependencyLevel;
        this.dependencyStatus = dependencyStatus;
    }

    public DependencyLevel getDependencyLevel() {
        return dependencyLevel;
    }

    public DependencyStatus getDependencyStatus() {
        return dependencyStatus;
    }

    public Long getUpstreamTaskRunId() {
        return upstreamTaskRunId;
    }

    public Long getDownStreamTaskRunId() {
        return downStreamTaskRunId;
    }
}
