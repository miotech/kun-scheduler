package com.miotech.kun.metadata.model.bo;

import com.miotech.kun.metadata.constant.DataStoreType;
import com.miotech.kun.metadata.model.DataStore;

public class PostgresDataStore extends DataStore {

    private String database;

    private String tableName;

    public PostgresDataStore(DataStoreType type) {
        super(type);
    }
}
