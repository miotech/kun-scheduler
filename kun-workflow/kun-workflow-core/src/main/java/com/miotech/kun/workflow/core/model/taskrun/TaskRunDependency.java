package com.miotech.kun.workflow.core.model.taskrun;

public class TaskRunDependency {

    private final Long upstreamTaskRunId;

    private final Long downStreamTaskRunId;

    public TaskRunDependency(Long upstreamTaskRunId,Long downStreamTaskRunId){
        this.upstreamTaskRunId = upstreamTaskRunId;
        this.downStreamTaskRunId = downStreamTaskRunId;
    }

    public Long getUpstreamTaskRunId() {
        return upstreamTaskRunId;
    }

    public Long getDownStreamTaskRunId() {
        return downStreamTaskRunId;
    }
}
