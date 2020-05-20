package com.miotech.kun.metadata.model.bo;

import com.miotech.kun.metadata.model.DataStore;

public class HiveDataStore extends DataStore {

    private String tableName;

    public HiveDataStore() {
    }

    public HiveDataStore(String tableName) {
        this.tableName = tableName;
    }
}
