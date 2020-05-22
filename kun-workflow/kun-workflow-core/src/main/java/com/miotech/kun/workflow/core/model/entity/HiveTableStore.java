package com.miotech.kun.workflow.core.model.entity;

public class HiveTableStore extends DataStore {

    private String database;

    private String table;

    public HiveTableStore() {
        super(DataStoreType.TABLE);
    }

    public HiveTableStore(String table) {
        this();
        this.table = table;
    }
}
