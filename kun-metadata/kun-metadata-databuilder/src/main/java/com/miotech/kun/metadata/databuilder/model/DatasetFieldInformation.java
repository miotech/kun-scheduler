package com.miotech.kun.metadata.databuilder.model;

public class DatasetFieldInformation {

    private long id;

    private String name;

    private String type;

    private String rawType;

    private boolean isPrimaryKey;

    private boolean isNullable;

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

    public boolean isPrimaryKey() {
        return isPrimaryKey;
    }

    public boolean isNullable() {
        return isNullable;
    }

    public DatasetFieldInformation(long id, String name, String type, String rawType, boolean isPrimaryKey, boolean isNullable) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.rawType = rawType;
        this.isPrimaryKey = isPrimaryKey;
        this.isNullable = isNullable;
    }
}
