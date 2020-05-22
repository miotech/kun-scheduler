package com.miotech.kun.metadata.model.bo;

import com.miotech.kun.metadata.constant.DataStoreType;
import com.miotech.kun.metadata.model.DataStore;

public class HiveDataStore extends DataStore {

    private String database;

    private String table;

    public HiveDataStore() {
        super(DataStoreType.TABLE);
    }

    public HiveDataStore(String table) {
        this();
        this.table = table;
    }
}
