package com.miotech.kun.metadata.core.model.connection;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.Objects;

@JsonTypeInfo(use=JsonTypeInfo.Id.NAME, include=JsonTypeInfo.As.EXISTING_PROPERTY,property = "connectionType",visible = true)
@JsonSubTypes(value = {
        @JsonSubTypes.Type(value = HiveServerConnectionInfo.class, name = "HIVE_SERVER"),
        @JsonSubTypes.Type(value = AthenaConnectionInfo.class, name = "ATHENA"),
        @JsonSubTypes.Type(value = HiveMetaStoreConnectionInfo.class, name = "HIVE_THRIFT"),
        @JsonSubTypes.Type(value = PostgresConnectionInfo.class, name = "POSTGRESQL"),
        @JsonSubTypes.Type(value = ArangoConnectionInfo.class, name = "ARANGO"),
        @JsonSubTypes.Type(value = S3ConnectionInfo.class, name = "S3"),
        @JsonSubTypes.Type(value = ESConnectionInfo.class, name = "ELASTICSEARCH"),
        @JsonSubTypes.Type(value = GlueConnectionInfo.class, name = "GLUE"),
        @JsonSubTypes.Type(value = MongoConnectionInfo.class, name = "MONGODB"),
        @JsonSubTypes.Type(value = MysqlConnectionInfo.class, name = "MYSQL")
})
public class ConnectionInfo {

    private final ConnectionType connectionType;

    public ConnectionInfo(ConnectionType connectionType) {
        this.connectionType = connectionType;
    }

    public ConnectionType getConnectionType() {
        return connectionType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConnectionInfo that = (ConnectionInfo) o;
        return connectionType == that.connectionType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(connectionType);
    }
}
