package com.miotech.kun.metadata.web.constant;

public enum TaskParam {
    MCE_TASK("mce-task"),
    MSE_TASK("mse-task")
    ;

    private String name;

    TaskParam(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static TaskParam convert(String name) {
        for (TaskParam value : values()) {
            if (value.getName().equals(name)) {
                return value;
            }
        }
        throw new IllegalArgumentException("TaskParam Not Found, name: " + name);
    }

}
