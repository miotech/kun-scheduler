package com.miotech.kun.dataplatform.model.commit;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public enum  CommitStatus {
    SUBMITTED,
    MIDDLE_VERSIONED,
    DEPLOYED;

    private static final Map<String, CommitStatus> mappings = new HashMap<>(16);

    static {
        for (CommitStatus commitStatus : values()) {
            mappings.put(commitStatus.name(), commitStatus);
        }
    }

    @Nullable
    public static CommitStatus resolve(@Nullable String status) {
        return (status != null ? mappings.get(status) : null);
    }
}
