package com.miotech.kun.metadata.model;

import com.miotech.kun.workflow.core.model.lineage.DataStore;
import com.miotech.kun.workflow.core.model.lineage.DataStoreType;

public class PostgresDataStore extends DataStore {

    private final String database;

    private final String tableName;

    public PostgresDataStore(DataStoreType type, String database, String tableName) {
        super(type);
        this.database = database;
        this.tableName = tableName;
    }
}
