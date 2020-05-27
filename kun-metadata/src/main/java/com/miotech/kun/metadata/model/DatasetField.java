package com.miotech.kun.metadata.model;

public class DatasetField {

    private final String name;

    private final String type;

    private final String comment;

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getComment() {
        return comment;
    }

    public DatasetField(String name, String type, String comment) {
        this.name = name;
        this.type = type;
        this.comment = comment;
    }

}
