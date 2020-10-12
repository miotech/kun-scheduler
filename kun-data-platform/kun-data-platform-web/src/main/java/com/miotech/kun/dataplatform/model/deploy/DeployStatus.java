package com.miotech.kun.dataplatform.model.deploy;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public enum  DeployStatus {
    CREATED,
    WORKING,
    FAILED,
    SUCCESS;

    private static final Map<String, DeployStatus> mappings = new HashMap<>(16);

    static {
        for (DeployStatus deployStatus : values()) {
            mappings.put(deployStatus.name(), deployStatus);
        }
    }

    @Nullable
    public static DeployStatus resolve(@Nullable String status) {
        return (status != null ? mappings.get(status) : null);
    }
}
