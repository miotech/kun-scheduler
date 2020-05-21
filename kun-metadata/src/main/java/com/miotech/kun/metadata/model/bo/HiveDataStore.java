package com.miotech.kun.metadata.model.bo;

import com.miotech.kun.metadata.constant.DataStoreType;
import com.miotech.kun.metadata.model.DataStore;

public class HiveDataStore extends DataStore {

    private String tableName;

    public HiveDataStore() {
        super(DataStoreType.A);
    }

    public HiveDataStore(String tableName) {
        this();
        this.tableName = tableName;
    }
}
