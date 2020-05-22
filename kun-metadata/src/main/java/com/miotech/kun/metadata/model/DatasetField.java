package com.miotech.kun.metadata.model;

public class DatasetField {

    private String name;

    private String type;

    private String description;

    private DatasetFieldStat datasetFieldStat;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public DatasetFieldStat getDatasetFieldStat() {
        return datasetFieldStat;
    }

    public void setDatasetFieldStat(DatasetFieldStat datasetFieldStat) {
        this.datasetFieldStat = datasetFieldStat;
    }

    public DatasetField(String type) {
        this.type = type;
    }

    public DatasetField(String name, String type, String description) {
        this.name = name;
        this.type = type;
        this.description = description;
    }
}
