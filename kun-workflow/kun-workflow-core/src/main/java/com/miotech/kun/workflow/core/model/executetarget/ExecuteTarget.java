package com.miotech.kun.workflow.core.model.executetarget;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;
import java.util.Objects;

public class ExecuteTarget {
    private final Long id;
    private final String name;
    private final Map<String, Object> properties;

    @JsonCreator
    public ExecuteTarget(@JsonProperty("id") Long id, @JsonProperty("name") String name,
                         @JsonProperty("properties") Map<String, Object> properties ) {
        this.id = id;
        this.name = name;
        this.properties = properties;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Object getProperty(String key) {
        return properties.get(key);
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public static ExecuteTargetBuilder newBuilder() {
        return new ExecuteTargetBuilder();
    }

    public ExecuteTargetBuilder cloneBuilder() {
        return ExecuteTarget.newBuilder()
                .withId(id)
                .withName(name)
                .withProperties(properties);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ExecuteTarget)) return false;
        ExecuteTarget that = (ExecuteTarget) o;
        return Objects.equals(getId(), that.getId()) &&
                Objects.equals(getName(), that.getName()) &&
                Objects.equals(properties, that.properties);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getName(), properties);
    }

    @Override
    public String toString() {
        return "ExecuteTarget{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }

    public static final class ExecuteTargetBuilder {
        private Long id;
        private String name;
        private Map<String, Object> properties;

        private ExecuteTargetBuilder() {
        }


        public ExecuteTargetBuilder withId(Long id) {
            this.id = id;
            return this;
        }

        public ExecuteTargetBuilder withName(String name) {
            this.name = name;
            return this;
        }

        public ExecuteTargetBuilder withProperties(Map<String, Object> properties) {
            this.properties = properties;
            return this;
        }

        public ExecuteTarget build() {
            ExecuteTarget executeTarget = new ExecuteTarget(id, name,properties);
            return executeTarget;
        }
    }
}
