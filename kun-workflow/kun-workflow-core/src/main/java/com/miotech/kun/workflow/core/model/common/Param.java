package com.miotech.kun.workflow.core.model.common;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import java.util.Objects;

@JsonDeserialize(builder = Param.ParamBuilder.class)
public class Param {
    private String name;
    private String description;
    private String value;

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getValue() {
        return value;
    }

    public static ParamBuilder newBuilder() {
        return new ParamBuilder();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Param param = (Param) o;
        return Objects.equals(name, param.name) &&
                Objects.equals(description, param.description) &&
                Objects.equals(value, param.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, description, value);
    }

    @JsonPOJOBuilder
    public static final class ParamBuilder {
        private String name;
        private String description;
        private String value;

        private ParamBuilder() {
        }

        public static ParamBuilder aParam() {
            return new ParamBuilder();
        }

        public ParamBuilder withName(String name) {
            this.name = name;
            return this;
        }

        public ParamBuilder withDescription(String description) {
            this.description = description;
            return this;
        }

        public ParamBuilder withValue(String value) {
            this.value = value;
            return this;
        }

        public Param build() {
            Param param = new Param();
            param.name = this.name;
            param.description = this.description;
            param.value = this.value;
            return param;
        }
    }
}
