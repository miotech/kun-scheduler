package com.miotech.kun.dataplatform.model.notify;

import com.fasterxml.jackson.annotation.JsonValue;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunStatus;

public enum TaskStatusNotifyTrigger {
    SYSTEM_DEFAULT("SYSTEM_DEFAULT"),
    ON_SUCCESS("ON_SUCCESS"),
    ON_FAIL("ON_FAIL"),
    ON_FINISH("ON_FINISH"),
    NEVER("NEVER");

    private final String typeName;

    @JsonValue
    public String getTypeName() {
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

    public boolean matches(TaskRunStatus toStatus) {
        switch (this) {
            case ON_SUCCESS:
                return toStatus.isSuccess();
            case ON_FAIL:
                return toStatus.isFailure();
            case ON_FINISH:
                return toStatus.isFinished();
            case NEVER:
                return false;
            default:
                break;
        }
        return toStatus.isFailure();
    }
}
