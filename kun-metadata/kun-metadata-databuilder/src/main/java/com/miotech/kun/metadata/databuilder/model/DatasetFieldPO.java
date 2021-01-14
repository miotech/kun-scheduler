package com.miotech.kun.metadata.databuilder.model;

public class DatasetFieldPO {

    private long id;

    private String name;

    private String type;

    private String rawType;

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getRawType() {
        return rawType;
    }

    public DatasetFieldPO(long id, String name, String type, String rawType) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.rawType = rawType;
    }

}
