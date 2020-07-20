package com.miotech.kun.metadata.web.constant;

import com.google.common.base.Preconditions;

public enum TaskParam {
    BUILD_ALL(WorkflowApiParam.TASK_NAME_BUILD_ALL, PropKey.OPERATOR_ID_BUILD_ALL, PropKey.TASK_ID_BUILD_ALL),
    REFRESH(WorkflowApiParam.TASK_NAME_REFRESH, PropKey.OPERATOR_ID_REFRESH, PropKey.TASK_ID_REFRESH);

    private String name;
    private String operatorKey;
    private String taskKey;

    public String getName() {
        return name;
    }

    public String getOperatorKey() {
        return operatorKey;
    }

    public String getTaskKey() {
        return taskKey;
    }

    TaskParam(String name, String operatorKey, String taskKey) {
        this.name = name;
        this.operatorKey = operatorKey;
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
