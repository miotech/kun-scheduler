package com.miotech.kun.workflow.core.model.lineage;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.miotech.kun.workflow.utils.JsonLongFieldDeserializer;

import java.util.Objects;

@JsonDeserialize(builder = EdgeTaskInfo.EdgeTaskInfoBuilder.class)
public class EdgeTaskInfo {
    @JsonSerialize(using= ToStringSerializer.class)
    @JsonDeserialize(using = JsonLongFieldDeserializer.class)
    private final Long id;

    private final String name;

    private final String description;

    public EdgeTaskInfo(Long id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
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

    public static EdgeTaskInfoBuilder newBuilder() {
        return new EdgeTaskInfoBuilder();
    }

    public EdgeTaskInfoBuilder cloneBuilder() {
        return new EdgeTaskInfoBuilder()
                .withId(id)
                .withName(name)
                .withDescription(description);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EdgeTaskInfo that = (EdgeTaskInfo) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(name, that.name) &&
                Objects.equals(description, that.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, description);
    }

    @JsonPOJOBuilder
    public static final class EdgeTaskInfoBuilder {
        private Long id;
        private String name;
        private String description;

        private EdgeTaskInfoBuilder() {
        }

        public static EdgeTaskInfoBuilder anEdgeTaskInfo() {
            return new EdgeTaskInfoBuilder();
        }

        public EdgeTaskInfoBuilder withId(Long id) {
            this.id = id;
            return this;
        }

        public EdgeTaskInfoBuilder withName(String name) {
            this.name = name;
            return this;
        }

        public EdgeTaskInfoBuilder withDescription(String description) {
            this.description = description;
            return this;
        }

        public EdgeTaskInfo build() {
            return new EdgeTaskInfo(id, name, description);
        }
    }
}
