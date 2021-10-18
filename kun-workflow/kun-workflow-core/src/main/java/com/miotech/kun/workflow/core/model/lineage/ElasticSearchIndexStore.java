package com.miotech.kun.workflow.core.model.lineage;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.miotech.kun.metadata.core.model.dataset.DataStore;
import com.miotech.kun.metadata.core.model.dataset.DataStoreType;
import com.miotech.kun.metadata.core.model.datasource.ConnectionInfo;

import java.util.HashMap;
import java.util.Map;

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
    public String getLocationInfo() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(index);
        return stringBuilder.toString();
    }

    @Override
    public ConnectionInfo getConnectionInfo() {
        Map<String,Object> values = new HashMap<>();
        values.put("host",getHost());
        values.put("port",getPort());
        return new ConnectionInfo(values);
    }

    @Override
    public String getName() {
        return index;
    }
}
