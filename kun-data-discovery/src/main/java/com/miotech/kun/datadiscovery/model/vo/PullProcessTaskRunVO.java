package com.miotech.kun.datadiscovery.model.vo;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.miotech.kun.workflow.client.model.Task;
import com.miotech.kun.workflow.client.model.TaskRun;
import com.miotech.kun.workflow.core.execution.Config;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunStatus;
import com.miotech.kun.workflow.utils.JsonLongFieldDeserializer;

import java.time.OffsetDateTime;
import java.util.List;

public class PullProcessTaskRunVO {
    @JsonSerialize(using = ToStringSerializer.class)
    @JsonDeserialize(using = JsonLongFieldDeserializer.class)
    private Long id;

    private Task task;

    private Config config;

    private TaskRunStatus status;

    private OffsetDateTime startAt;

    private OffsetDateTime endAt;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;

    @JsonSerialize(contentUsing = ToStringSerializer.class)
    private List<Long> dependentTaskRunIds;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    public Config getConfig() {
        return config;
    }

    public void setConfig(Config config) {
        this.config = config;
    }

    public TaskRunStatus getStatus() {
        return status;
    }

    public void setStatus(TaskRunStatus status) {
        this.status = status;
    }

    public OffsetDateTime getStartAt() {
        return startAt;
    }

    public void setStartAt(OffsetDateTime startAt) {
        this.startAt = startAt;
    }

    public OffsetDateTime getEndAt() {
        return endAt;
    }

    public void setEndAt(OffsetDateTime endAt) {
        this.endAt = endAt;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<Long> getDependentTaskRunIds() {
        return dependentTaskRunIds;
    }

    public void setDependentTaskRunIds(List<Long> dependentTaskRunIds) {
        this.dependentTaskRunIds = dependentTaskRunIds;
    }

    public static PullProcessTaskRunVO fromTaskRun(TaskRun taskRun) {
        PullProcessTaskRunVO vo = new PullProcessTaskRunVO();
        vo.setId(taskRun.getId());
        vo.setConfig(taskRun.getConfig());
        vo.setTask(taskRun.getTask());
        vo.setStartAt(taskRun.getStartAt());
        vo.setEndAt(taskRun.getEndAt());
        vo.setDependentTaskRunIds(taskRun.getDependencyTaskRunIds());
        vo.setCreatedAt(taskRun.getCreatedAt());
        vo.setUpdatedAt(taskRun.getUpdatedAt());
        return vo;
    }
}
