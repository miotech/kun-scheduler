package com.miotech.kun.workflow.core.model.task;

import com.google.common.collect.ImmutableMap;
import com.miotech.kun.workflow.core.model.common.Variable;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RunTaskContext {
    public static final RunTaskContext EMPTY = new RunTaskContext(Collections.emptyMap());

    private final Map<Long, List<Variable>> configuredVariablesMap;

    private RunTaskContext(Map<Long, List<Variable>> configuredVariablesMap) {
        this.configuredVariablesMap = ImmutableMap.copyOf(configuredVariablesMap);
    }

    public List<Variable> getConfiguredVariables(Long taskId) {
        return configuredVariablesMap.getOrDefault(taskId, Collections.emptyList());
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {
        private final Map<Long, List<Variable>> map = new HashMap<>();

        private Builder() {
        }

        public Builder addVariables(Long taskId, Map<String, String> configuredVariables) {
            List<Variable> variableList = configuredVariables.entrySet().stream()
                    .map(e -> Variable.of(e.getKey(), e.getValue(), null))
                    .collect(Collectors.toList());
            map.put(taskId, variableList);
            return this;
        }

        public RunTaskContext build() {
            return new RunTaskContext(map);
        }
    }
}
