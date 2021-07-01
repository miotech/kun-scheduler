package com.miotech.kun.workflow.core.model.lineage;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.miotech.kun.metadata.core.model.dataset.DSI;
import com.miotech.kun.metadata.core.model.dataset.DataStore;
import com.miotech.kun.metadata.core.model.dataset.DataStoreType;

public class PostgresDataStore extends DataStore {

    private final String host;

    private final int port;

    private final String database;

    private final String schema;

    private final String tableName;



    @JsonCreator
    public PostgresDataStore(@JsonProperty("host") String host,
                             @JsonProperty("port") int port,
                             @JsonProperty("database") String database,
                             @JsonProperty("schema") String schema,
                             @JsonProperty("tableName") String tableName) {
        super(DataStoreType.POSTGRES_TABLE);
        this.host = host;
        this.port = port;
        this.database = database;
        this.schema = schema;
        this.tableName = tableName;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getDatabase() {
        return database;
    }

    public String getSchema() {
        return schema;
    }

    public String getTableName() {
        return tableName;
    }

    @Override
    public String getDatabaseName() {
        return String.format("%s.%s", database, schema);
    }

    @Override
    public DSI getDSI() {
        return DSI.newBuilder()
                .withStoreType("postgres")
                .putProperty("host", host)
                .putProperty("port", String.valueOf(port))
                .putProperty("database", database)
                .putProperty("schema", schema)
                .putProperty("table", tableName)
                .build();
    }
}
