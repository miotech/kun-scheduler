package com.miotech.kun.workflow.core.model.entity;

public class DataStore {

    private final DataStoreType type;
    private Cluster cluster;

    public DataStoreType getType() {
        return type;
    }

    public Cluster getCluster() {
        return cluster;
    }

    public DataStore(DataStoreType type, Cluster cluster) {
        this.type = type;
        this.cluster = cluster;
    }
}
