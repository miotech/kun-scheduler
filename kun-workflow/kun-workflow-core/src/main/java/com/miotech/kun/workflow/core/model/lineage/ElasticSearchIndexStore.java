package com.miotech.kun.workflow.core.model.lineage;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.miotech.kun.workflow.core.model.common.URI;

public class ElasticSearchIndexStore extends DataStore{
    private final String dataStoreUrl;

    private final String index;

    public String getDataStoreUrl() {
        return dataStoreUrl;
    }

    public String getIndex() {
        return index;
    }

    @JsonCreator
    public ElasticSearchIndexStore(@JsonProperty("dataStoreUrl") String dataStoreUrl,
                          @JsonProperty("index") String index) {
        super(DataStoreType.ELASTICSEARCH_INDEX);
        this.dataStoreUrl = dataStoreUrl;
        this.index = index;
    }

    @Override
    public String getDatabaseName() {
        return null;
    }

    @Override
    public URI getURI() {
        return URI.from(this.dataStoreUrl + "/" + index);
    }
}
