package com.miotech.kun.metadata.core.model.vo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class DatasetDetail extends DatasetBasicInfo {

    private Long rowCount;

    public DatasetDetail() {
    }

    @JsonCreator
    public DatasetDetail(@JsonProperty("rowCount") Long rowCount) {
        this.rowCount = rowCount;
    }

    public Long getRowCount() {
        return rowCount;
    }

    public void setRowCount(Long rowCount) {
        this.rowCount = rowCount;
    }
}
