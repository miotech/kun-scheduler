package com.miotech.kun.metadata.model;

public class DatasetFieldPO {

    private long id;

    private String type;

    public long getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public DatasetFieldPO(long id, String type) {
        this.id = id;
        this.type = type;
    }


}
