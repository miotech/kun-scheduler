package com.miotech.kun.metadata.model;

import java.util.Objects;

public class DatasetField {

    private final String name;

    private final DatasetFieldType fieldType;

    private final String comment;

    public String getName() {
        return name;
    }

    public DatasetFieldType getFieldType() {
        return fieldType;
    }

    public String getComment() {
        return comment;
    }

    public DatasetField(String name, DatasetFieldType fieldType, String comment) {
        this.name = name;
        this.fieldType = fieldType;
        this.comment = comment;
    }

    @Override
    public String toString() {
        return "DatasetField{" +
                "name='" + name + '\'' +
                ", fieldType=" + fieldType +
                ", comment='" + comment + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DatasetField field = (DatasetField) o;
        return Objects.equals(name, field.name) &&
                Objects.equals(fieldType, field.fieldType) &&
                Objects.equals(comment, field.comment);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, fieldType, comment);
    }
}
