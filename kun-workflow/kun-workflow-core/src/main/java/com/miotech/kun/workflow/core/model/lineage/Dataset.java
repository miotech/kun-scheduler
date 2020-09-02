package com.miotech.kun.workflow.core.model.lineage;

public class Dataset {
    Long gid;
    String name;
    Schema schema;
    DataStore dataStore;

    public Dataset() {
    }

    public Dataset(Long gid) {
        this.gid = gid;
    }

    public Dataset(Long gid, String datasetName) {
        this.gid = gid;
        this.name = datasetName;
    }

    public Long getGid() {
        return gid;
    }

    public void setGid(Long gid) {
        this.gid = gid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Schema getSchema() {
        return schema;
    }

    public void setSchema(Schema schema) {
        this.schema = schema;
    }

    public DataStore getDataStore() {
        return dataStore;
    }

    public void setDataStore(DataStore dataStore) {
        this.dataStore = dataStore;
    }
}
