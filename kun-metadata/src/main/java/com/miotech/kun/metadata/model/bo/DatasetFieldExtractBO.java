package com.miotech.kun.metadata.model.bo;

public class DatasetFieldExtractBO extends DatasetExtractBO {

    private Long datasetId;

    private String tableName;

    public Long getDatasetId() {
        return datasetId;
    }

    public void setDatasetId(Long datasetId) {
        this.datasetId = datasetId;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }
}
