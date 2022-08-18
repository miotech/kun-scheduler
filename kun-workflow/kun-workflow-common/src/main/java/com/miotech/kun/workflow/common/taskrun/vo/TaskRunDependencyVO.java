package com.miotech.kun.workflow.common.taskrun.vo;

public class TaskRunDependencyVO {
    public final Long downstreamTaskRunId;

    public final Long upstreamTaskRunId;

    public TaskRunDependencyVO(Long downstreamTaskRunId, Long upstreamTaskRunId) {
        this.downstreamTaskRunId = downstreamTaskRunId;
        this.upstreamTaskRunId = upstreamTaskRunId;
    }
}
