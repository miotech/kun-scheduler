package com.miotech.kun.metadata.model;

public class DatasetField {

    private final String name;

    private final String type;

    private final String comment;

    private DatasetFieldStat datasetFieldStat;

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getComment() {
        return comment;
    }

    public DatasetFieldStat getDatasetFieldStat() {
        return datasetFieldStat;
    }

    public void setDatasetFieldStat(DatasetFieldStat datasetFieldStat) {
        this.datasetFieldStat = datasetFieldStat;
    }

    public DatasetField(String name, String type, String comment) {
        this.name = name;
        this.type = type;
        this.comment = comment;
    }

    public DatasetField(String name, String type, String comment, DatasetFieldStat datasetFieldStat) {
        this.name = name;
        this.type = type;
        this.comment = comment;
        this.datasetFieldStat = datasetFieldStat;
    }

}
