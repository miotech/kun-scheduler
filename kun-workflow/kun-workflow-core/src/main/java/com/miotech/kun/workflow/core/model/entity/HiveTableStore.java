package com.miotech.kun.workflow.core.model.entity;

public class HiveTableStore extends DataStore {

    private String database;

    private String table;

    public HiveTableStore(String database, String table) {
        super(DataStoreType.TABLE);
        this.database = database;
        this.table = table;
    }
}
