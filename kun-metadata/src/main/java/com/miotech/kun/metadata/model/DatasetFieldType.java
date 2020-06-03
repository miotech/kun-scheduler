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
        STRUCT,
        ARRAY,
        DATETIME,
        BOOLEAN
    }

    public static Type convertRawType(String rawType) {
        if ("string".equals(rawType) || "STRING".equals(rawType) || rawType.startsWith("varchar") || rawType.startsWith("char")) {
            return Type.CHARACTER;
        } else if ("timestamp".equals(rawType)) {
            return Type.DATETIME;
        } else if (rawType.startsWith("array") || "ARRAY".equals(rawType)) {
            return Type.ARRAY;
        } else if (rawType.startsWith("decimal") || "double".equals(rawType) || "number".equals(rawType) ||
                "NUMBER".equals(rawType) || "int".equals(rawType) || "bigint".equals(rawType)) {
            return Type.NUMBER;
        } else if (rawType.startsWith("struct")) {
            return Type.STRUCT;
        } else if ("boolean".equals(rawType) || "BOOL".equals(rawType)) {
            return Type.BOOLEAN;
        } else {
            throw new RuntimeException("unknown type: " + rawType);
        }
    }
}
