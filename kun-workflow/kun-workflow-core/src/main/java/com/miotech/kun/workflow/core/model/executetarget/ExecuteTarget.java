package com.miotech.kun.workflow.core.model.executetarget;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class ExecuteTarget {
    private final Long id;
    private final String name;

    @JsonCreator
    public ExecuteTarget(@JsonProperty("id") Long id, @JsonProperty("name") String name) {
        this.id = id;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public static ExecuteTargetBuilder newBuilder() {
        return new ExecuteTargetBuilder();
    }

    public ExecuteTargetBuilder cloneBuilder() {
        return ExecuteTarget.newBuilder()
                .withId(id)
                .withName(name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ExecuteTarget)) return false;
        ExecuteTarget that = (ExecuteTarget) o;
        return Objects.equals(getId(), that.getId()) &&
                getName().equals(that.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getName());
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

        public ExecuteTarget build() {
            return new ExecuteTarget(id, name);
        }
    }
}
