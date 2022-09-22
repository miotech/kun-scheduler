package com.miotech.kun.metadata.core.model.connection;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.Objects;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "connectionType", visible = true)
@JsonSubTypes(value = {
        @JsonSubTypes.Type(value = HiveServerConnectionConfigInfo.class, name = "HIVE_SERVER"),
        @JsonSubTypes.Type(value = AthenaConnectionConfigInfo.class, name = "ATHENA"),
        @JsonSubTypes.Type(value = HiveMetaStoreConnectionConfigInfo.class, name = "HIVE_THRIFT"),
        @JsonSubTypes.Type(value = PostgresConnectionConfigInfo.class, name = "POSTGRESQL"),
        @JsonSubTypes.Type(value = ArangoConnectionConfigInfo.class, name = "ARANGO"),
        @JsonSubTypes.Type(value = S3ConnectionConfigInfo.class, name = "S3"),
        @JsonSubTypes.Type(value = ESConnectionConfigInfo.class, name = "ELASTICSEARCH"),
        @JsonSubTypes.Type(value = GlueConnectionConfigInfo.class, name = "GLUE"),
        @JsonSubTypes.Type(value = MongoConnectionConfigInfo.class, name = "MONGODB"),
        @JsonSubTypes.Type(value = MysqlConnectionConfigInfo.class, name = "MYSQL"),
        @JsonSubTypes.Type(value = HDFSConnectionConfigInfo.class, name = "HDFS")
})
public class ConnectionConfigInfo {

    private final ConnectionType connectionType;

    public ConnectionConfigInfo(ConnectionType connectionType) {
        this.connectionType = connectionType;
    }

    public ConnectionType getConnectionType() {
        return connectionType;
    }

    public boolean sameDatasource(Object o) {
        if (Objects.isNull(o)) {
            return false;
        }
        return equals(o);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConnectionConfigInfo that = (ConnectionConfigInfo) o;
        return connectionType == that.connectionType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(connectionType);
    }
}
