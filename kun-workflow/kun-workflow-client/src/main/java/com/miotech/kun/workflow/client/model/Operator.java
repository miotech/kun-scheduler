package com.miotech.kun.workflow.client.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.miotech.kun.workflow.core.model.common.Param;

import java.util.List;
import java.util.Objects;

@JsonDeserialize(builder = Operator.Builder.class)
public class Operator {
    private final Long id;
    private final String name;
    private final String description;
    private final String packagePath;
    private final String className;
    private final List<Param> params;

    public Operator(Long id, String name, String description, String packagePath, String className, List<Param> params) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.packagePath = packagePath;
        this.className = className;
        this.params = params;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getPackagePath() {
        return packagePath;
    }

    public String getClassName() {
        return className;
    }

    public List<Param> getParams() {
        return params;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public Builder cloneBuilder() {
        return new Builder()
                .withName(name)
                .withClassName(className)
                .withDescription(description)
                .withParams(params)
                .withPackagePath(packagePath);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Operator that = (Operator) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(description, that.description) &&
                Objects.equals(packagePath, that.packagePath) &&
                Objects.equals(className, that.className) &&
                Objects.equals(params, that.params);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, description, packagePath, className, params);
    }

    @JsonPOJOBuilder
    public static final class Builder {
        private Long id;
        private String name;
        private String description;
        private String packagePath;
        private String className;
        private List<Param> params;

        private Builder() {
        }

        public static Builder anOperatorInfoVO() {
            return new Builder();
        }

        public Builder withId(Long id) {
            this.id = id;
            return this;
        }

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public Builder withDescription(String description) {
            this.description = description;
            return this;
        }

        public Builder withPackagePath(String packagePath) {
            this.packagePath = packagePath;
            return this;
        }

        public Builder withClassName(String className) {
            this.className = className;
            return this;
        }

        public Builder withParams(List<Param> params) {
            this.params = params;
            return this;
        }

        public Operator build() {
            return new Operator(id, name, description, packagePath, className, params);
        }
    }
}
