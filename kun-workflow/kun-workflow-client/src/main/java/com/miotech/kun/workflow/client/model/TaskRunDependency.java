package com.miotech.kun.workflow.client.model;

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
}
