package com.miotech.kun.workflow.core.model.lineage;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class MongoDataStore extends DataStore {

    private final String url;

    private final String database;

    private final String collection;

    public String getUrl() {
        return url;
    }

    public String getDatabase() {
        return database;
    }

    public String getCollection() {
        return collection;
    }

    @JsonCreator
    public MongoDataStore(@JsonProperty("url") String url,
                          @JsonProperty("database") String database,
                          @JsonProperty("collection") String collection) {
        super(DataStoreType.COLLECTION);
        this.url = url;
        this.database = database;
        this.collection = collection;
    }
}
