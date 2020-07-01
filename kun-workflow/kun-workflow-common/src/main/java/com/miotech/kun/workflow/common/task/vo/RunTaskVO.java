package com.miotech.kun.workflow.common.task.vo;

import com.google.common.collect.Maps;

import java.util.Map;

public class RunTaskVO {
    private Long taskId;

    private Map<String, String> variables = Maps.newHashMap();

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public Map<String, String> getVariables() {
        return variables;
    }

    public void setVariables(Map<String, String> variables) {
        this.variables = variables;
    }
}
