package com.miotech.kun.workflow.client.model;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class RunTaskRequest {
    private List<RunTaskInfo> runTasks = new ArrayList<>();

    private Long targetId;

    private OffsetDateTime scheduleTime;

    public void addTaskConfig(Long taskId, Map<String, Object> overwriteConfig) {
        RunTaskInfo runTaskInfo = new RunTaskInfo();
        runTaskInfo.setTaskId(taskId);
        runTaskInfo.setConfig(overwriteConfig);
        runTasks.add(runTaskInfo);
    }

    public List<RunTaskInfo> getRunTasks() {
        return runTasks;
    }

    public Long getTargetId() {
        return targetId;
    }

    public void setTargetId(Long targetId) {
        this.targetId = targetId;
    }

    public void setScheduleTime(OffsetDateTime scheduleTime) {
        this.scheduleTime = scheduleTime;
    }

    public OffsetDateTime getScheduleTime() {
        return scheduleTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RunTaskRequest)) return false;
        RunTaskRequest that = (RunTaskRequest) o;
        return Objects.equals(getRunTasks(), that.getRunTasks()) &&
                Objects.equals(getTargetId(), that.getTargetId()) &&
                Objects.equals(getScheduleTime(), that.getScheduleTime());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getRunTasks(), getTargetId(), getScheduleTime());
    }
}
