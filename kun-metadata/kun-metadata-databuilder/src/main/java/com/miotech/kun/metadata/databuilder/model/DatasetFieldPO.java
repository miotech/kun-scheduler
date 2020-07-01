package com.miotech.kun.metadata.databuilder.model;

public class DatasetFieldPO {

    private long id;

    private String name;

    private String type;

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public DatasetFieldPO(long id, String name, String type) {
        this.id = id;
        this.name = name;
        this.type = type;
    }


}
