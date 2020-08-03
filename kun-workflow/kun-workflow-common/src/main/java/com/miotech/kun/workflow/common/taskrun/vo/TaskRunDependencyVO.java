package com.miotech.kun.workflow.common.taskrun.vo;

public class TaskRunDependencyVO {
    public final Long downStreamTaskRunId;

    public final Long upstreamTaskRunId;

    public TaskRunDependencyVO(Long downStreamTaskRunId, Long upstreamTaskRunId) {
        this.downStreamTaskRunId = downStreamTaskRunId;
        this.upstreamTaskRunId = upstreamTaskRunId;
    }
}
