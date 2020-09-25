package com.miotech.kun.dataplatform.model.tasktemplate;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(builder = ParameterDefinition.Builder.class)
public class ParameterDefinition {

    final String name;

    final String type;

    final boolean required;

    final String displayName;

    public ParameterDefinition(String name, String type, boolean required, String displayName) {
        this.name = name;
        this.type = type;
        this.required = required;
        this.displayName = displayName;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public boolean isRequired() {
        return required;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static Builder newBuilder() { return new Builder(); }

    public static final class Builder {
        String name;
        String type;
        boolean required;
        String displayName;

        private Builder() {
        }

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public Builder withType(String type) {
            this.type = type;
            return this;
        }

        public Builder withRequired(boolean required) {
            this.required = required;
            return this;
        }

        public Builder withDisplayName(String displayName) {
            this.displayName = displayName;
            return this;
        }

        public ParameterDefinition build() {
            return new ParameterDefinition(name, type, required, displayName);
        }
    }
}
