package com.miotech.kun.workflow.core.model.taskrun;

public class TaskRunUpstreamDependency {

    private final Long upstreamTaskRunId;

    private final TaskRunStatus upstreamStatus;

    public TaskRunUpstreamDependency(Long upstreamTaskRunIds,TaskRunStatus upstreamStatus){
        this.upstreamTaskRunId = upstreamTaskRunIds;
        this.upstreamStatus = upstreamStatus;
    }

}
