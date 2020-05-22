package com.miotech.kun.workflow.core.model.entity;

public class MongoDataStore extends DataStore {

    private String collectionName;

    public MongoDataStore() {
        super(DataStoreType.COLLECTION);
    }
}
