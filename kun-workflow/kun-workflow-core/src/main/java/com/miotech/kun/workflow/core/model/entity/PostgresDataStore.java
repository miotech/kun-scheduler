package com.miotech.kun.workflow.core.model.entity;

public class PostgresDataStore extends DataStore {

    private String database;

    private String tableName;

    public PostgresDataStore(DataStoreType type) {
        super(type);
    }
}
