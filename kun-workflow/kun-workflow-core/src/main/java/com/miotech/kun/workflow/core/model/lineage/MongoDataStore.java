package com.miotech.kun.workflow.core.model.lineage;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.miotech.kun.metadata.core.model.DSI;
import com.miotech.kun.metadata.core.model.DataStore;
import com.miotech.kun.metadata.core.model.DataStoreType;

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
        super(DataStoreType.MONGO_COLLECTION);
        this.url = url;
        this.database = database;
        this.collection = collection;
    }

    @Override
    public String getDatabaseName() {
        return getDatabase();
    }

    @Override
    public DSI getDSI() {
        return DSI.newBuilder().withStoreType("mongodb")
                .putProperty("url", url)
                .putProperty("database", database)
                .putProperty("collection", collection)
                .build();
    }
}
