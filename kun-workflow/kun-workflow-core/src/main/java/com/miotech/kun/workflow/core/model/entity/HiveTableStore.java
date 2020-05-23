package com.miotech.kun.workflow.core.model.entity;

public class HiveTableStore extends DataStore {

    private final String database;

    private final String table;

    public HiveTableStore(String database, String table, HiveCluster cluster) {
        super(DataStoreType.TABLE, cluster);
        this.database = database;
        this.table = table;
    }
}
