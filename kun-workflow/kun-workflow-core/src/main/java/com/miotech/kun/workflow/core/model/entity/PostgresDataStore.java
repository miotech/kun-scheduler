package com.miotech.kun.workflow.core.model.entity;

public class PostgresDataStore extends DataStore {

    private final String database;

    private final String tableName;

    public PostgresDataStore(DataStoreType type, PostgresCluster cluster, String database, String tableName) {
        super(type, cluster);
        this.database = database;
        this.tableName = tableName;
    }
}
