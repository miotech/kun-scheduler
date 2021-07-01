package com.miotech.kun.workflow.core.model.lineage;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.miotech.kun.metadata.core.model.dataset.DSI;
import com.miotech.kun.metadata.core.model.dataset.DataStore;
import com.miotech.kun.metadata.core.model.dataset.DataStoreType;

public class ElasticSearchIndexStore extends DataStore {

    private final String host;

    private final int port;

    private final String index;

    @JsonCreator
    public ElasticSearchIndexStore(@JsonProperty("host") String host,
                                   @JsonProperty("port") int port,
                                   @JsonProperty("index") String index) {
        super(DataStoreType.ELASTICSEARCH_INDEX);
        this.host = host;
        this.port = port;
        this.index = index;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getIndex() {
        return index;
    }

    @Override
    public String getDatabaseName() {
        return null;
    }

    @Override
    public DSI getDSI() {
        return DSI.newBuilder().withStoreType("elasticsearch")
                .putProperty("host", host)
                .putProperty("port", String.valueOf(port))
                .putProperty("index", index)
                .build();
    }
}
