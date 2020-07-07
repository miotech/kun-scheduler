package com.miotech.kun.workflow.core.model.lineage;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ArangoCollectionStore extends DataStore{

    private final String dataStoreUrl;

    private final String database;

    private final String collection;

    public String getDataStoreUrl() {
        return dataStoreUrl;
    }

    public String getDatabase() {
        return database;
    }

    public String getCollection() {
        return collection;
    }

    @JsonCreator
    public ArangoCollectionStore(@JsonProperty("dataStoreUrl") String dataStoreUrl,
                          @JsonProperty("database") String database,
                          @JsonProperty("collection") String collection) {
        super(DataStoreType.ARANGO_COLLECTION);
        this.dataStoreUrl = dataStoreUrl;
        this.database = database;
        this.collection = collection;
    }

    @Override
    public String getDatabaseName() {
        return getDatabase();
    }
}
