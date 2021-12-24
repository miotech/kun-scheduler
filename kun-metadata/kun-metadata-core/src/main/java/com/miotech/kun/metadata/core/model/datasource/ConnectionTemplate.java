package com.miotech.kun.metadata.core.model.datasource;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.miotech.kun.metadata.core.model.connection.ConnectionType;

import java.util.List;

@JsonDeserialize(builder = ConnectionTemplate.Builder.class)
public class ConnectionTemplate {

    private final ConnectionType connectionType;

    private final List<Field> fields;

    public ConnectionTemplate(ConnectionType connectionType, List<Field> fields) {
        this.connectionType = connectionType;
        this.fields = fields;
    }

    public ConnectionType getConnectionType() {
        return connectionType;
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

    public ConnectionTemplate.Builder cloneBuilder() {
        return ConnectionTemplate.newBuilder()
                .withConnectionType(connectionType)
                .withFields(fields)
                ;
    }

    @JsonPOJOBuilder
    public static final class Builder {
        private ConnectionType connectionType;
        private List<Field> fields;

        private Builder() {
        }


        public Builder withConnectionType(ConnectionType connectionType) {
            this.connectionType = connectionType;
            return this;
        }

        public Builder withFields(List<Field> fields) {
            this.fields = fields;
            return this;
        }

        public ConnectionTemplate build() {
            return new ConnectionTemplate(connectionType, fields);
        }
    }
}
