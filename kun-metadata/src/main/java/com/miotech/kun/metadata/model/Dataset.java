package com.miotech.kun.metadata.model;

import java.util.List;

public class Dataset {

    private String name;

    private Long databaseId;

    private DataStore dataStore;

    private List<DatasetField> fields;

    private DatasetStat datasetStat;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getDatabaseId() {
        return databaseId;
    }

    public void setDatabaseId(Long databaseId) {
        this.databaseId = databaseId;
    }

    public DataStore getDataStore() {
        return dataStore;
    }

    public void setDataStore(DataStore dataStore) {
        this.dataStore = dataStore;
    }

    public List<DatasetField> getFields() {
        return fields;
    }

    public void setFields(List<DatasetField> fields) {
        this.fields = fields;
    }

    public DatasetStat getDatasetStat() {
        return datasetStat;
    }

    public void setDatasetStat(DatasetStat datasetStat) {
        this.datasetStat = datasetStat;
    }
}
