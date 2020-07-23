package com.miotech.kun.metadata.web.model.vo;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import java.util.Objects;

@JsonDeserialize(builder = OperatorPropsVO.OperatorPropsVOBuilder.class)
public class OperatorPropsVO {
    private final String name;
    private final String description;
    private final String packagePath;
    private final String className;

    public OperatorPropsVO(String name, String description, String packagePath, String className) {
        this.name = name;
        this.description = description;
        this.packagePath = packagePath;
        this.className = className;
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

    public static OperatorPropsVOBuilder newBuilder() {
        return new OperatorPropsVOBuilder();
    }

    public OperatorPropsVOBuilder cloneBuilder() {
        return new OperatorPropsVOBuilder()
                .withName(name)
                .withClassName(className)
                .withDescription(description)
                .withPackagePath(packagePath);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OperatorPropsVO that = (OperatorPropsVO) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(description, that.description) &&
                Objects.equals(packagePath, that.packagePath) &&
                Objects.equals(className, that.className);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, description, packagePath, className);
    }

    @JsonPOJOBuilder
    public static final class OperatorPropsVOBuilder {
        private String name;
        private String description;
        private String packagePath;
        private String className;

        private OperatorPropsVOBuilder() {
        }

        public OperatorPropsVOBuilder withName(String name) {
            this.name = name;
            return this;
        }

        public OperatorPropsVOBuilder withDescription(String description) {
            this.description = description;
            return this;
        }

        public OperatorPropsVOBuilder withPackagePath(String packagePath) {
            this.packagePath = packagePath;
            return this;
        }

        public OperatorPropsVOBuilder withClassName(String className) {
            this.className = className;
            return this;
        }

        public OperatorPropsVO build() {
            return new OperatorPropsVO(name, description, packagePath, className);
        }
    }
}
