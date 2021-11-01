package com.miotech.kun.workflow.common.task.vo;

import java.util.List;

public class RunTasksParams {
    List<RunTaskVO> runTasks;
    Long targetId;

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
}
