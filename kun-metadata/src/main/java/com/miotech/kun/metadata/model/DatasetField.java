package com.miotech.kun.metadata.model;

import java.util.Objects;

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

    @Override
    public String toString() {
        return "DatasetField{" +
                "name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", comment='" + comment + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DatasetField that = (DatasetField) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(type, that.type) &&
                Objects.equals(comment, that.comment);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, type, comment);
    }
}
