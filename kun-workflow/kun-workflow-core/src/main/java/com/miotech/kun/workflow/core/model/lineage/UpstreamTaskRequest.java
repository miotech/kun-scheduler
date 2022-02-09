package com.miotech.kun.workflow.core.model.lineage;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class UpstreamTaskRequest {

    private final List<Long> datasetGids;

    @JsonCreator
    public UpstreamTaskRequest(@JsonProperty("datasetGids") List<Long> datasetGids) {
        this.datasetGids = datasetGids;
    }

    public List<Long> getDatasetGids() {
        return datasetGids;
    }
}
