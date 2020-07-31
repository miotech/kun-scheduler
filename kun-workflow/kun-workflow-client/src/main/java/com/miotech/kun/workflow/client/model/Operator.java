package com.miotech.kun.workflow.client.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.miotech.kun.workflow.core.execution.ConfigDef;
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

    private final String className;

    private final List<ConfigKey> configDef;

    private Operator(Long id,
                     String name,
                     String description,
                     String className,
                     List<ConfigKey> configDef
                     ) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.className = className;
        this.configDef = configDef;
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


    public List<ConfigKey> getConfigDef() {
        return configDef;
    }

    public static OperatorBuilder newBuilder() {
        return new OperatorBuilder();
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
                Objects.equals(configDef, operator.configDef);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, description, className);
    }

    @JsonPOJOBuilder
    public static final class OperatorBuilder {
        private Long id;
        private String name;
        private String description;
        private String className;
        private String packagePath;
        private List<ConfigKey> configDef;

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

        public OperatorBuilder withConfigDef(List<ConfigKey> configKeys) {
            this.configDef = configKeys;
            return this;
        }

        public Operator build() {
            return new Operator(id, name, description, className, configDef);
        }
    }
}
