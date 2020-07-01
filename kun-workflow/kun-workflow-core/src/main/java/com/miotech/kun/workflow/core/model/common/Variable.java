package com.miotech.kun.workflow.core.model.common;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import java.util.Objects;

@JsonDeserialize(builder = Variable.VariableBuilder.class)
public class Variable {
    private String key;
    private String value;
    private String defaultValue;

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public Variable withValue(String value) {
        return cloneBuilder().withValue(value).build();
    }

    public static Variable of(String key, String value, String defaultValue) {
        return newBuilder()
                .withKey(key)
                .withValue(value)
                .withDefaultValue(defaultValue)
                .build();
    }

    public static VariableBuilder newBuilder() {
        return new VariableBuilder();
    }

    public VariableBuilder cloneBuilder() {
        return new VariableBuilder()
                .withKey(key)
                .withValue(value)
                .withDefaultValue(defaultValue);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Variable variable = (Variable) o;
        return Objects.equals(key, variable.key) &&
                Objects.equals(value, variable.value) &&
                Objects.equals(defaultValue, variable.defaultValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, value, defaultValue);
    }

    @JsonPOJOBuilder
    public static final class VariableBuilder {
        private String key;
        private String value;
        private String defaultValue;

        private VariableBuilder() {
        }

        public static VariableBuilder aVariableDef() {
            return new VariableBuilder();
        }

        public VariableBuilder withKey(String key) {
            this.key = key;
            return this;
        }

        public VariableBuilder withValue(String value) {
            this.value = value;
            return this;
        }

        public VariableBuilder withDefaultValue(String defaultValue) {
            this.defaultValue = defaultValue;
            return this;
        }

        public Variable build() {
            Variable variable = new Variable();
            variable.key = this.key;
            variable.value = this.value;
            variable.defaultValue = this.defaultValue;
            return variable;
        }
    }
}
