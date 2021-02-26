package com.miotech.kun.dataplatform.model.notify;

import com.fasterxml.jackson.annotation.JsonValue;

public enum TaskStatusNotifyTrigger {
    SYSTEM_DEFAULT("SYSTEM_DEFAULT"),
    ON_SUCCESS("ON_SUCCESS"),
    ON_FAIL("ON_FAIL"),
    ON_FINISH("ON_FINISH"),
    NEVER("NEVER");

    private final String typeName;

    @JsonValue
    public String getTriggerType() {
        return this.typeName;
    }

    TaskStatusNotifyTrigger(String typeName) {
        this.typeName = typeName;
    }

    public static TaskStatusNotifyTrigger from(String value) {
        switch (value) {
            case "SYSTEM_DEFAULT":
                return SYSTEM_DEFAULT;
            case "ON_SUCCESS":
                return ON_SUCCESS;
            case "ON_FAIL":
                return ON_FAIL;
            case "ON_FINISH":
                return ON_FINISH;
            case "NEVER":
                return NEVER;
            default:
                throw new IllegalArgumentException(String.format("Unknown task status notifier trigger type: \"%s\"", value));
        }
    }
}
