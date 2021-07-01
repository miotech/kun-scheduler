package com.miotech.kun.metadata.core.model.datasource;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import java.util.List;

@JsonDeserialize(builder = DataSourceType.Builder.class)
public class DataSourceType {

    private final Long id;

    private final String name;

    private final List<Field> fields;

    public DataSourceType(Long id, String name, List<Field> fields) {
        this.id = id;
        this.name = name;
        this.fields = fields;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<Field> getFields() {
        return fields;
    }

    public static class Field {

        private final String format;

        private final boolean require;

        private final String name;

        private final int sequenceOrder;

        @JsonCreator
        public Field(@JsonProperty("format") String format,
                     @JsonProperty("require") boolean require,
                     @JsonProperty("name") String name,
                     @JsonProperty("sequenceOrder") int sequenceOrder) {
            this.format = format;
            this.require = require;
            this.name = name;
            this.sequenceOrder = sequenceOrder;
        }

        public String getFormat() {
            return format;
        }

        public boolean isRequire() {
            return require;
        }

        public String getName() {
            return name;
        }

        public int getSequenceOrder() {
            return sequenceOrder;
        }

    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public DataSourceType.Builder cloneBuilder() {
        return DataSourceType.newBuilder()
                .withId(id)
                .withName(name)
                .withFields(fields)
                ;
    }

    @JsonPOJOBuilder
    public static final class Builder {
        private Long id;
        private String name;
        private List<Field> fields;

        private Builder() {
        }

        public Builder withId(Long id) {
            this.id = id;
            return this;
        }

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public Builder withFields(List<Field> fields) {
            this.fields = fields;
            return this;
        }

        public DataSourceType build() {
            return new DataSourceType(id, name, fields);
        }
    }
}
