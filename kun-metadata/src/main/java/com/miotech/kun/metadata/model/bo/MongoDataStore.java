package com.miotech.kun.metadata.model.bo;

import com.miotech.kun.metadata.constant.DataStoreType;
import com.miotech.kun.metadata.model.DataStore;

public class MongoDataStore extends DataStore {

    private String collectionName;

    public MongoDataStore() {
        super(DataStoreType.A);
    }
}
