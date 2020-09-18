package com.miotech.kun.metadata.web.constant;

import com.google.common.base.Preconditions;

public enum TaskParam {
    AUTO(WorkflowApiParam.DATA_BUILDER_TASK_AUTO, PropKey.TASK_ID_AUTO),
    MANUAL(WorkflowApiParam.DATA_BUILDER_TASK_MANUAL, PropKey.TASK_ID_MANUAL);

    private String name;
    private String taskKey;

    public String getName() {
        return name;
    }

    public String getTaskKey() {
        return taskKey;
    }

    TaskParam(String name, String taskKey) {
        this.name = name;
        this.taskKey = taskKey;
    }

    public static TaskParam get(String name) {
        Preconditions.checkNotNull(name);
        for (TaskParam value : values()) {
            if (name.equals(value.name)) {
                return value;
            }
        }
        throw new IllegalArgumentException("TaskParam Not Found, name: " + name);
    }

}
