package com.miotech.kun.metadata.core.model.vo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class DatasetBasicSearch extends PageInfo {

    private List<DatasetBasicInfo> datasets;

    public DatasetBasicSearch() {
    }

    @JsonCreator
    public DatasetBasicSearch(@JsonProperty("datasets") List<DatasetBasicInfo> datasets) {
        this.datasets = datasets;
    }

    public List<DatasetBasicInfo> getDatasets() {
        return datasets;
    }

    public void setDatasets(List<DatasetBasicInfo> datasets) {
        this.datasets = datasets;
    }

}
