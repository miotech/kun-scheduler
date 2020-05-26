package com.miotech.kun.metadata.model;

import com.miotech.kun.workflow.core.model.lineage.DataStore;
import com.miotech.kun.workflow.core.model.lineage.DataStoreType;

public class MongoDataStore extends DataStore {

    private final String collectionName;

    public MongoDataStore(DataStoreType type, String collectionName) {
        super(type);
        this.collectionName = collectionName;
    }
}
