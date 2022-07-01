package com.miotech.kun.workflow.common.task.vo;

import java.time.OffsetDateTime;
import java.util.List;

public class RunTasksParams {
    List<RunTaskVO> runTasks;
    Long targetId;

    OffsetDateTime scheduleTime;

    public List<RunTaskVO> getRunTaskVOs() {
        return runTasks;
    }

    public void setRunTaskVOs(List<RunTaskVO> runTaskVOs) {
        this.runTasks = runTaskVOs;
    }

    public Long getTargetId() {
        return targetId;
    }

    public void setTargetId(Long targetId) {
        this.targetId = targetId;
    }

    public OffsetDateTime getScheduleTime() {
        return scheduleTime;
    }

    public void setScheduleTime(OffsetDateTime scheduleTime) {
        this.scheduleTime = scheduleTime;
    }
}
