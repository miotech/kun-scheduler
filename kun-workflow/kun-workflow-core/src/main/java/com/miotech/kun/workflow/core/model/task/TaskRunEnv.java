package com.miotech.kun.workflow.core.model.task;

import com.google.common.collect.ImmutableMap;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class TaskRunEnv {
    public static final TaskRunEnv EMPTY = new TaskRunEnv(Collections.emptyMap());

    private final Map<Long, Map<String, String>> configMap;

    private TaskRunEnv(Map<Long, Map<String, String>> configMap) {
        this.configMap = ImmutableMap.copyOf(configMap);
    }

    public Map<String, String> getConfig(Long taskId) {
        return configMap.getOrDefault(taskId, Collections.emptyMap());
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static final class Builder {
        private Map<Long, Map<String, String>> configMap = new HashMap<>();

        private Builder() {
        }

        public Builder addConfig(Long taskId, Map<String, String> config) {
            configMap.put(taskId, config);
            return this;
        }

        public TaskRunEnv build() {
            return new TaskRunEnv(configMap);
        }
    }
}
