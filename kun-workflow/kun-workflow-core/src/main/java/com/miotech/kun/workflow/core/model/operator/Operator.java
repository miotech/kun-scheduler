package com.miotech.kun.workflow.core.model.operator;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.miotech.kun.workflow.core.model.common.Param;
import com.miotech.kun.workflow.utils.JsonLongFieldDeserializer;

import java.util.List;
import java.util.Objects;

@JsonDeserialize(builder = Operator.OperatorBuilder.class)
public class Operator {
    @JsonSerialize(using = ToStringSerializer.class)
    @JsonDeserialize(using = JsonLongFieldDeserializer.class)
    private final Long id;

    private final String name;

    private final String description;

    private final List<Param> params;

    private final String className;

    private final String packagePath;

    private Operator(Long id, String name, String description, List<Param> params, String className, String packagePath) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.params = params;
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

    public List<Param> getParams() {
        return params;
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
                .withParams(params)
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
                Objects.equals(params, operator.params) &&
                Objects.equals(className, operator.className) &&
                Objects.equals(packagePath, operator.packagePath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, description, params, className, packagePath);
    }

    @JsonPOJOBuilder
    public static final class OperatorBuilder {
        private Long id;
        private String name;
        private String description;
        private List<Param> params;
        private String className;
        private String packagePath;

        private OperatorBuilder() {
        }

        public static OperatorBuilder anOperator() {
            return new OperatorBuilder();
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

        public OperatorBuilder withParams(List<Param> params) {
            this.params = params;
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
            return new Operator(id, name, description, params, className, packagePath);
        }
    }
}
