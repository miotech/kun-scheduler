package com.miotech.kun.workflow.core.model.task;

import com.google.common.collect.ImmutableMap;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class TaskRunEnv {
    public static final TaskRunEnv EMPTY = new TaskRunEnv(Collections.emptyMap(),null);

    private final Map<Long, Map<String, Object>> configMap;

    private final Long targetId;


    private TaskRunEnv(Map<Long, Map<String, Object>> configMap,Long targetId) {
        this.configMap = ImmutableMap.copyOf(configMap);
        this.targetId = targetId;
    }

    public Long getTargetId(){
        return targetId;
    }

    public Map<String, Object> getConfig(Long taskId) {
        return configMap.getOrDefault(taskId, Collections.emptyMap());
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static final class Builder {
        private Map<Long, Map<String, Object>> configMap = new HashMap<>();
        private Long targetId;

        private Builder() {
        }

        public Builder addConfig(Long taskId, Map<String, Object> config) {
            configMap.put(taskId, config);
            return this;
        }

        public Builder setTargetId(Long targetId){
            this.targetId = targetId;
            return this;
        }

        public TaskRunEnv build() {
            return new TaskRunEnv(configMap,targetId);
        }
    }
}
