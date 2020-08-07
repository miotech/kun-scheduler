package com.miotech.kun.workflow.common.task.vo;

import com.google.common.collect.Maps;

import java.util.Map;

public class RunTaskVO {
    private Long taskId;

    private Map<String, Object> config = Maps.newHashMap();

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public Map<String, Object> getConfig() {
        return config;
    }

    public void setConfig(Map<String, Object> config) {
        this.config = config;
    }
}
