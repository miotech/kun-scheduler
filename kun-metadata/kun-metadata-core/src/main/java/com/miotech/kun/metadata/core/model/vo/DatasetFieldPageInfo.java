package com.miotech.kun.metadata.core.model.vo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class DatasetFieldPageInfo extends PageInfo {

    private List<DatasetFieldInfo> columns;

    public DatasetFieldPageInfo() {
    }

    @JsonCreator
    public DatasetFieldPageInfo(@JsonProperty("columns") List<DatasetFieldInfo> columns) {
        this.columns = columns;
    }

    public List<DatasetFieldInfo> getColumns() {
        return columns;
    }

    public void setColumns(List<DatasetFieldInfo> columns) {
        this.columns = columns;
    }
}
