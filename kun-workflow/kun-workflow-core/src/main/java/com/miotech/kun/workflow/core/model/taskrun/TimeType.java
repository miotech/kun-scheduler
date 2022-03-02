package com.miotech.kun.workflow.core.model.taskrun;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 *  TimeType is the selection type for task run time range
 */
public enum TimeType {
    startAt,
    createdAt,
    queuedAt,
    endAt;

    private static final Map<String, TimeType> mappings = new HashMap<>(4);

    static {
        for (TimeType type : values()) {
            mappings.put(type.name(), type);
        }
    }

    @Nullable
    public static TimeType resolve(@Nullable String type) {
        return (type != null ? mappings.get(type) : null);
    }

}
