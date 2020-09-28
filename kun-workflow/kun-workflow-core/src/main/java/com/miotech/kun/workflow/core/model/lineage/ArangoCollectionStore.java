package com.miotech.kun.workflow.core.model.lineage;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.miotech.kun.metadata.core.common.URI;
import com.miotech.kun.metadata.core.model.DataStore;

import static com.miotech.kun.metadata.core.model.DataStoreType.ARANGO_COLLECTION;

public class ArangoCollectionStore extends DataStore {

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
        super(ARANGO_COLLECTION);
        this.dataStoreUrl = dataStoreUrl;
        this.database = database;
        this.collection = collection;
    }

    @Override
    public String getDatabaseName() {
        return getDatabase();
    }

    @Override
    public URI getURI() {
        return URI.from(dataStoreUrl + "/" + database + "?collection=" + collection);
    }
}
