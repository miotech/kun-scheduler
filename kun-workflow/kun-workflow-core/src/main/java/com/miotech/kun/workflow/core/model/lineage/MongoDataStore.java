package com.miotech.kun.workflow.core.model.lineage;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.miotech.kun.metadata.core.model.dataset.DSI;
import com.miotech.kun.metadata.core.model.dataset.DataStore;
import com.miotech.kun.metadata.core.model.dataset.DataStoreType;

public class MongoDataStore extends DataStore {

    private final String host;

    private final int port;

    private final String database;

    private final String collection;

    @JsonCreator
    public MongoDataStore(@JsonProperty("host") String host,
                          @JsonProperty("port") int port,
                          @JsonProperty("database") String database,
                          @JsonProperty("collection") String collection) {
        super(DataStoreType.MONGO_COLLECTION);
        this.host = host;
        this.port = port;
        this.database = database;
        this.collection = collection;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getDatabase() {
        return database;
    }

    public String getCollection() {
        return collection;
    }

    @Override
    public String getDatabaseName() {
        return getDatabase();
    }

    @Override
    public DSI getDSI() {
        return DSI.newBuilder().withStoreType("mongodb")
                .putProperty("host", host)
                .putProperty("port", String.valueOf(port))
                .putProperty("database", database)
                .putProperty("collection", collection)
                .build();
    }
}
