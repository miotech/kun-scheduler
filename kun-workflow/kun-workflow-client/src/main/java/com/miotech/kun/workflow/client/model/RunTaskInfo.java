package com.miotech.kun.workflow.client.model;

import java.util.Map;

public class RunTaskInfo {
        private long taskId;

        private Map<String, Object> config;

        public RunTaskInfo() {}

        public RunTaskInfo(long taskId, Map<String, Object> config) {
            this.taskId = taskId;
            this.config = config;
        }

    public long getTaskId() {
        return taskId;
    }

    public void setTaskId(long taskId) {
        this.taskId = taskId;
    }

    public Map<String, Object> getConfig() {
        return config;
    }

    public void setConfig(Map<String, Object> config) {
        this.config = config;
    }
}