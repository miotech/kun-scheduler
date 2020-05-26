package com.miotech.kun.workflow.core.model.lineage;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class MongoDataStore extends DataStore {

    private final String collectionName;

    public String getCollectionName() {
        return collectionName;
    }

    @JsonCreator
    public MongoDataStore(@JsonProperty("collectionName") String collectionName) {
        super(DataStoreType.COLLECTION);
        this.collectionName = collectionName;
    }
}
