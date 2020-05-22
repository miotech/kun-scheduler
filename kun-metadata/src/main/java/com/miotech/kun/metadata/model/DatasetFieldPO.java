package com.miotech.kun.metadata.model;

public class DatasetFieldPO extends DatasetField {

    private long id;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public DatasetFieldPO(long id, String type) {
        super(type);
        this.id = id;
    }


}
