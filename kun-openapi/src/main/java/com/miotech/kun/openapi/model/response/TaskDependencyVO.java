package com.miotech.kun.openapi.model.response;

public class TaskDependencyVO {

    private final Long downstreamTaskId;

    private final Long upstreamTaskId;

    public TaskDependencyVO(Long downstreamTaskId, Long upstreamTaskId) {
        this.downstreamTaskId = downstreamTaskId;
        this.upstreamTaskId = upstreamTaskId;
    }

    public Long getDownstreamTaskId() {
        return downstreamTaskId;
    }

    public Long getUpstreamTaskId() {
        return upstreamTaskId;
    }
}
