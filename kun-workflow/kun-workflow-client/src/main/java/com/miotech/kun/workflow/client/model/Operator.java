package com.miotech.kun.workflow.client.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.miotech.kun.workflow.utils.JsonLongFieldDeserializer;

import java.util.Objects;

@JsonDeserialize(builder = Operator.OperatorBuilder.class)
public class Operator {
    @JsonSerialize(using = ToStringSerializer.class)
    @JsonDeserialize(using = JsonLongFieldDeserializer.class)
    private final Long id;

    private final String name;

    private final String description;

    private final String className;

    private final String packagePath;

    private Operator(Long id, String name, String description, String className, String packagePath) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.className = className;
        this.packagePath = packagePath;
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

    public String getClassName() {
        return className;
    }

    public String getPackagePath() {
        return packagePath;
    }

    public static OperatorBuilder newBuilder() {
        return new OperatorBuilder();
    }

    public OperatorBuilder cloneBuilder() {
        return new OperatorBuilder()
                .withId(id)
                .withName(name)
                .withDescription(description)
                .withClassName(className)
                .withPackagePath(packagePath);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Operator operator = (Operator) o;
        return Objects.equals(id, operator.id) &&
                Objects.equals(name, operator.name) &&
                Objects.equals(description, operator.description) &&
                Objects.equals(className, operator.className) &&
                Objects.equals(packagePath, operator.packagePath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, description, className, packagePath);
    }

    @JsonPOJOBuilder
    public static final class OperatorBuilder {
        private Long id;
        private String name;
        private String description;
        private String className;
        private String packagePath;

        private OperatorBuilder() {
        }

        public OperatorBuilder withId(Long id) {
            this.id = id;
            return this;
        }

        public OperatorBuilder withName(String name) {
            this.name = name;
            return this;
        }

        public OperatorBuilder withDescription(String description) {
            this.description = description;
            return this;
        }

        public OperatorBuilder withClassName(String className) {
            this.className = className;
            return this;
        }

        public OperatorBuilder withPackagePath(String packagePath) {
            this.packagePath = packagePath;
            return this;
        }

        public Operator build() {
            return new Operator(id, name, description, className, packagePath);
        }
    }
}
