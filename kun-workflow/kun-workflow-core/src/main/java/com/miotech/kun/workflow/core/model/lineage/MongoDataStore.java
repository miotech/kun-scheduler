package com.miotech.kun.workflow.core.model.lineage;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.miotech.kun.metadata.core.model.dataset.DataStore;
import com.miotech.kun.metadata.core.model.dataset.DataStoreType;
import com.miotech.kun.metadata.core.model.connection.ConnectionConfigInfo;
import com.miotech.kun.metadata.core.model.connection.ConnectionType;
import com.miotech.kun.metadata.core.model.connection.MongoConnectionConfigInfo;

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
    public String getLocationInfo() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(database).append(":").append(collection);
        return stringBuilder.toString();
    }

    @Override
    public ConnectionConfigInfo getConnectionConfigInfo() {
        return new MongoConnectionConfigInfo(ConnectionType.MONGODB, host, port);
    }

    @Override
    public String getName() {
        return collection;
    }
}
