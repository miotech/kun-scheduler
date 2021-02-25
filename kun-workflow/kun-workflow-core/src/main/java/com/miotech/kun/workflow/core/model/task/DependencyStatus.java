package com.miotech.kun.workflow.core.model.task;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

public enum DependencyStatus {
    CREATED,
    FAILED,
    SUCCESS;

    private static Map<String, DependencyStatus> statusMaps = ImmutableMap.of(CREATED.name(), CREATED,
            FAILED.name(), FAILED, SUCCESS.name(), SUCCESS);

    public static DependencyStatus resolve(String status) {
        return statusMaps.get(status);
    }
}
