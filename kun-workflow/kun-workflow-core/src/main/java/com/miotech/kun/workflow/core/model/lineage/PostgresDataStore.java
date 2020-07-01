package com.miotech.kun.workflow.core.model.lineage;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class PostgresDataStore extends DataStore {

    private final String url;

    private final String database;

    private final String schema;

    private final String tableName;

    public String getUrl() {
        return url;
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

    @JsonCreator
    public PostgresDataStore(@JsonProperty("url") String url,
                             @JsonProperty("database") String database,
                             @JsonProperty("schema") String schema,
                             @JsonProperty("tableName") String tableName) {
        super(DataStoreType.POSTGRES_TABLE);
        this.url = url;
        this.database = database;
        this.schema = schema;
        this.tableName = tableName;
    }
}
