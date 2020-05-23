package com.miotech.kun.workflow.core.model.entity;

public class MongoDataStore extends DataStore {

    private final String collectionName;

    public MongoDataStore(DataStoreType type, Cluster cluster, String collectionName) {
        super(type, cluster);
        this.collectionName = collectionName;
    }
}
