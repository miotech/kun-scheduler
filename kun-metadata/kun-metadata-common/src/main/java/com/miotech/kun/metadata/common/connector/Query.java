package com.miotech.kun.metadata.common.connector;

public class Query {

    private final String database;
    private final String schema;
    private final String sql;

    public Query(String database, String schema, String sql) {
        this.database = database;
        this.schema = schema;
        this.sql = sql;
    }

    public String getDatabase() {
        return database;
    }

    public String getSchema() {
        return schema;
    }

    public String getSql() {
        return sql;
    }
}
