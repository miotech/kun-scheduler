package com.miotech.kun.metadata.core.model.dataset;

public class DatasetTag {

    private final Long id;

    private final Long datasetGid;

    private final String tag;

    public DatasetTag(Long id, Long datasetGid, String tag) {
        this.id = id;
        this.datasetGid = datasetGid;
        this.tag = tag;
    }

    public Long getId() {
        return id;
    }

    public Long getDatasetGid() {
        return datasetGid;
    }

    public String getTag() {
        return tag;
    }

}
