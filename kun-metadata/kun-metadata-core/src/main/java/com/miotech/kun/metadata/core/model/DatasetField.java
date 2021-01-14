package com.miotech.kun.metadata.core.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;
import java.util.Objects;

public class DatasetField implements Serializable {
    @JsonIgnore
    private static final long serialVersionUID = -1603335359239L;

    private final String name;

    private final DatasetFieldType fieldType;

    private final String comment;

    private final boolean isPrimaryKey;

    private final boolean isNullable;

    public String getName() {
        return name;
    }

    public DatasetFieldType getFieldType() {
        return fieldType;
    }

    public String getComment() {
        return comment;
    }

    public boolean isPrimaryKey() {
        return isPrimaryKey;
    }

    public boolean isNullable() {
        return isNullable;
    }

    public DatasetField(String name, DatasetFieldType fieldType, String comment) {
        this(name, fieldType, comment, false, true);
    }

    public DatasetField(String name, DatasetFieldType fieldType, String comment, boolean isPrimaryKey, boolean isNullable) {
        this.name = name;
        this.fieldType = fieldType;
        this.comment = comment;
        this.isPrimaryKey = isPrimaryKey;
        this.isNullable = isNullable;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DatasetField that = (DatasetField) o;
        return isPrimaryKey == that.isPrimaryKey &&
                isNullable == that.isNullable &&
                Objects.equals(name, that.name) &&
                Objects.equals(fieldType, that.fieldType) &&
                Objects.equals(comment, that.comment);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, fieldType, comment, isPrimaryKey, isNullable);
    }

    @Override
    public String toString() {
        return "DatasetField{" +
                "name='" + name + '\'' +
                ", fieldType=" + fieldType +
                ", comment='" + comment + '\'' +
                ", isPrimaryKey=" + isPrimaryKey +
                ", isNullable=" + isNullable +
                '}';
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static final class Builder {
        private String name;
        private DatasetFieldType fieldType;
        private String comment;
        private boolean isPrimaryKey;
        private boolean isNullable;

        private Builder() {
        }

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public Builder withFieldType(DatasetFieldType fieldType) {
            this.fieldType = fieldType;
            return this;
        }

        public Builder withComment(String comment) {
            this.comment = comment;
            return this;
        }

        public Builder withIsPrimaryKey(boolean isPrimaryKey) {
            this.isPrimaryKey = isPrimaryKey;
            return this;
        }

        public Builder withIsNullable(boolean isNullable) {
            this.isNullable = isNullable;
            return this;
        }

        public DatasetField build() {
            return new DatasetField(name, fieldType, comment, isPrimaryKey, isNullable);
        }
    }
}
