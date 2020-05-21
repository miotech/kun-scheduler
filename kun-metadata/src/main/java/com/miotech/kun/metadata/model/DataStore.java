package com.miotech.kun.metadata.model;

import com.miotech.kun.metadata.constant.DataStoreType;

public class DataStore {

    private final DataStoreType type;
    private Database database;

    public DataStoreType getType() {
        return type;
    }

    public Database getDatabase() {
        return database;
    }

    public DataStore(DataStoreType type) {
        this.type = type;
    }

}
