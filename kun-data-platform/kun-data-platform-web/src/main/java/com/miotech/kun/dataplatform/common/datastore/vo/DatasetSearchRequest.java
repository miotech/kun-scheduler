package com.miotech.kun.dataplatform.common.datastore.vo;

import com.google.common.collect.ImmutableList;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = false)
@Data
public class DatasetSearchRequest {
    private final List<Long> definitionIds;

    private final List<Long> datastoreIds;

    private final String datasetName;

    public DatasetSearchRequest(List<Long> definitionIds,
                                List<Long> datastoreIds,
                                String datasetName) {
        this.definitionIds = definitionIds == null ? ImmutableList.of() : definitionIds;
        this.datastoreIds = datastoreIds == null ? ImmutableList.of() : datastoreIds;
        this.datasetName = datasetName;
    }
}
