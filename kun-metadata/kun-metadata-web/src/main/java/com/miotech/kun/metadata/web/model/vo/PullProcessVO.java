package com.miotech.kun.metadata.web.model.vo;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.miotech.kun.metadata.core.model.process.PullProcessType;
import com.miotech.kun.workflow.client.model.TaskRun;
import com.miotech.kun.workflow.utils.JsonLongFieldDeserializer;

import java.time.OffsetDateTime;

public class PullProcessVO {
    @JsonSerialize(using = ToStringSerializer.class)
    @JsonDeserialize(using = JsonLongFieldDeserializer.class)
    private Long processId;

    private PullProcessType processType;

    private OffsetDateTime createdAt;

    private TaskRun latestMCETaskRun;

    // Currently should be null
    // TODO: Figure out how to get this task run instance for dataset pulling tasks
    private TaskRun latestMSETaskRun;

    public PullProcessVO() {
    }

    public PullProcessVO(Long processId, PullProcessType processType, OffsetDateTime createdAt, TaskRun latestMCETaskRun, TaskRun latestMSETaskRun) {
        this.processId = processId;
        this.processType = processType;
        this.createdAt = createdAt;
        this.latestMCETaskRun = latestMCETaskRun;
        this.latestMSETaskRun = latestMSETaskRun;
    }

    public Long getProcessId() {
        return processId;
    }

    public void setProcessId(Long processId) {
        this.processId = processId;
    }

    public PullProcessType getProcessType() {
        return processType;
    }

    public void setProcessType(PullProcessType processType) {
        this.processType = processType;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public TaskRun getLatestMCETaskRun() {
        return latestMCETaskRun;
    }

    public void setLatestMCETaskRun(TaskRun latestMCETaskRun) {
        this.latestMCETaskRun = latestMCETaskRun;
    }

    public TaskRun getLatestMSETaskRun() {
        return latestMSETaskRun;
    }

    public void setLatestMSETaskRun(TaskRun latestMSETaskRun) {
        this.latestMSETaskRun = latestMSETaskRun;
    }
}
