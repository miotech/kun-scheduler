package com.miotech.kun.workflow.common.task.vo;

import java.util.List;

public class RunTasksParams {
    List<RunTaskVO> runTaskVOs;
    Long targetId;

    public List<RunTaskVO> getRunTaskVOs() {
        return runTaskVOs;
    }

    public void setRunTaskVOs(List<RunTaskVO> runTaskVOs) {
        this.runTaskVOs = runTaskVOs;
    }

    public Long getTargetId() {
        return targetId;
    }

    public void setTargetId(Long targetId) {
        this.targetId = targetId;
    }
}
