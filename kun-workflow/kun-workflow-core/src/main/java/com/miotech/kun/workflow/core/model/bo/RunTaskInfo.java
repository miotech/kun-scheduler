package com.miotech.kun.workflow.core.model.bo;

public class RunTaskInfo {

    private long taskId;

    private String variables;

    public long getTaskId() {
        return taskId;
    }

    public void setTaskId(long taskId) {
        this.taskId = taskId;
    }

    public String getVariables() {
        return variables;
    }

    public void setVariables(String variables) {
        this.variables = variables;
    }
}
