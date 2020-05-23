package com.miotech.kun.metadata.model;

public class DatasetFieldType {
    private final Type type;
    private final String rawType;

    public DatasetFieldType(Type type, String rawType) {
        this.type = type;
        this.rawType = rawType;
    }

    public Type getType() {
        return type;
    }

    public String getRawType() {
        return rawType;
    }

    public enum Type {
        NUMBER,
        CHARACTER,
        BINARY,
        DATETIME
    }
}
