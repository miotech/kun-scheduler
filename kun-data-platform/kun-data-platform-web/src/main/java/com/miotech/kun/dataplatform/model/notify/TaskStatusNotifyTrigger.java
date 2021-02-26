package com.miotech.kun.dataplatform.model.notify;

import com.fasterxml.jackson.annotation.JsonValue;

public enum TaskStatusNotifyTrigger {
    SYSTEM_DEFAULT("SYSTEM_DEFAULT"),
    SUCCESS("SUCCESS"),
    FAILED("FAILED"),
    FINISHED("FINISHED"),
    NEVER("NEVER");

    private final String typeName;

    @JsonValue
    public String getTriggerType() {
        return this.typeName;
    }

    TaskStatusNotifyTrigger(String typeName) {
        this.typeName = typeName;
    }
}
