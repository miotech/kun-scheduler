package com.miotech.kun.datadiscovery.model.entity;

import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.miotech.kun.metadata.core.model.connection.ConnectionType;

import java.util.List;

public class ConnectionTemplateVO {
    private final ConnectionType connectionType;

    private final List<ConnectionTypeField> fields;

    public ConnectionTemplateVO(ConnectionType connectionType, List<ConnectionTypeField> fields) {
        this.connectionType = connectionType;
        this.fields = fields;
    }

    public ConnectionType getConnectionType() {
        return connectionType;
    }

    public List<ConnectionTypeField> getFields() {
        return fields;
    }
    public static ConnectionTemplateVO.Builder newBuilder() {
        return new ConnectionTemplateVO.Builder();
    }

    public ConnectionTemplateVO.Builder cloneBuilder() {
        return ConnectionTemplateVO.newBuilder()
                .withConnectionType(connectionType)
                .withFields(fields)
                ;
    }

    @JsonPOJOBuilder
    public static final class Builder {
        private ConnectionType connectionType;
        private List<ConnectionTypeField> fields;

        private Builder() {
        }


        public ConnectionTemplateVO.Builder withConnectionType(ConnectionType connectionType) {
            this.connectionType = connectionType;
            return this;
        }

        public ConnectionTemplateVO.Builder withFields(List<ConnectionTypeField> fields) {
            this.fields = fields;
            return this;
        }

        public ConnectionTemplateVO build() {
            return new ConnectionTemplateVO(connectionType, fields);
        }
    }
}
