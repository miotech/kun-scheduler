package com.miotech.kun.workflow.client.model;

import java.util.Map;

public class RunTaskInfo {
        private long taskId;

        private Map<String, String> variables;

        public RunTaskInfo() {}

        public RunTaskInfo(long taskId, Map<String, String> variables) {
            this.taskId = taskId;
            this.variables = variables;
        }

    public long getTaskId() {
        return taskId;
    }

    public void setTaskId(long taskId) {
        this.taskId = taskId;
    }

    public Map<String, String> getVariables() {
        return variables;
    }

    public void setVariables(Map<String, String> variables) {
        this.variables = variables;
    }
}