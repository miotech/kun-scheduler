package com.miotech.kun.metadata.model.bo;

public class HiveDataStore extends BaseDataStore {

    private String tableName;

    public HiveDataStore() {
    }

    public HiveDataStore(String tableName) {
        this.tableName = tableName;
    }
}
