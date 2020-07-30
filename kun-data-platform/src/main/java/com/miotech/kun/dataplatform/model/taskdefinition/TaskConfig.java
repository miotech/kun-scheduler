package com.miotech.kun.dataplatform.model.taskdefinition;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.Map;

@JsonDeserialize(builder = TaskConfig.Builder.class)
public class TaskConfig {
    private final Map<String, Object> params;

    private final Map<String, Object> variableDefs;

    public TaskConfig(Map<String, Object> params, Map<String, Object> variableDefs) {
        this.params = params;
        this.variableDefs = variableDefs;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public Map<String, Object> getVariableDefs() {
        return variableDefs;
    }

    public static Builder newBuilder() { return new Builder(); }

    public static final class Builder {
        private Map<String, Object> params;
        private Map<String, Object> variableDefs;

        private Builder() {
        }

        public Builder withParams(Map<String, Object> params) {
            this.params = params;
            return this;
        }

        public Builder withVariableDefs(Map<String, Object> variableDefs) {
            this.variableDefs = variableDefs;
            return this;
        }

        public TaskConfig build() {
            return new TaskConfig(params, variableDefs);
        }
    }
}
