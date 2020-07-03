package com.miotech.kun.workflow.common.resource;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public enum ResourceType {
    FILE("file"),
    JAR("jar");

    private final String type;

    ResourceType(String type) {
        this.type = type;
    }

    private static final Map<String, ResourceType> mappings = new HashMap<>(16);

    static {
        for (ResourceType resourceType : values()) {
            mappings.put(resourceType.type, resourceType);
        }
    }

    public String getType() { return this.type; }

    @Nullable
    public static ResourceType resolve(@Nullable String type) {
        return (type != null ? mappings.get(type) : null);
    }

}
