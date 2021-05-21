package com.miotech.kun.metadata.web.model.vo;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.miotech.kun.metadata.core.model.process.PullProcessType;
import com.miotech.kun.workflow.client.CustomDateTimeDeserializer;
import com.miotech.kun.workflow.client.CustomDateTimeSerializer;
import com.miotech.kun.workflow.client.model.TaskRun;
import com.miotech.kun.workflow.utils.JsonLongFieldDeserializer;

import java.time.OffsetDateTime;

public class PullProcessVO {
    @JsonSerialize(using = ToStringSerializer.class)
    @JsonDeserialize(using = JsonLongFieldDeserializer.class)
    private Long processId;

    private PullProcessType processType;

    @JsonDeserialize(using = CustomDateTimeDeserializer.class)
    @JsonSerialize(using = CustomDateTimeSerializer.class)
    private OffsetDateTime createdAt;

    private PullProcessTaskRunVO latestMCETaskRun;

    // Currently should be null
    // TODO: Figure out how to get this task run instance for dataset pulling tasks
    private PullProcessTaskRunVO latestMSETaskRun;

    public PullProcessVO() {
    }

    public PullProcessVO(Long processId, PullProcessType processType, OffsetDateTime createdAt, TaskRun latestMCETaskRun, TaskRun latestMSETaskRun) {
        this.processId = processId;
        this.processType = processType;
        this.createdAt = createdAt;
        this.latestMCETaskRun = latestMCETaskRun != null ? PullProcessTaskRunVO.fromTaskRun(latestMCETaskRun) : null;
        this.latestMSETaskRun = latestMSETaskRun != null ? PullProcessTaskRunVO.fromTaskRun(latestMSETaskRun) : null;
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

    public PullProcessTaskRunVO getLatestMCETaskRun() {
        return latestMCETaskRun;
    }

    public void setLatestMCETaskRun(PullProcessTaskRunVO latestMCETaskRun) {
        this.latestMCETaskRun = latestMCETaskRun;
    }

    public PullProcessTaskRunVO getLatestMSETaskRun() {
        return latestMSETaskRun;
    }

    public void setLatestMSETaskRun(PullProcessTaskRunVO latestMSETaskRun) {
        this.latestMSETaskRun = latestMSETaskRun;
    }
}
