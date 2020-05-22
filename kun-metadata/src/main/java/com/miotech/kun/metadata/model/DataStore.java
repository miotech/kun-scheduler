package com.miotech.kun.metadata.model;

import com.miotech.kun.metadata.constant.DataStoreType;

public class DataStore {

    private final DataStoreType type;
    private Cluster cluster;

    public DataStoreType getType() {
        return type;
    }

    public Cluster getCluster() {
        return cluster;
    }

    public DataStore(DataStoreType type) {
        this.type = type;
    }

}
