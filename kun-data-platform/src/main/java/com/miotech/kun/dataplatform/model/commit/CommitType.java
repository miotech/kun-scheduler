package com.miotech.kun.dataplatform.model.commit;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public enum  CommitType {
    CREATED,
    MODIFIED,
    OFFLINE;

    private static final Map<String, CommitType> mappings = new HashMap<>(16);

    static {
        for (CommitType commitType : values()) {
            mappings.put(commitType.name(), commitType);
        }
    }

    @Nullable
    public static CommitType resolve(@Nullable String commitType) {
        return (commitType != null ? mappings.get(commitType) : null);
    }
}
