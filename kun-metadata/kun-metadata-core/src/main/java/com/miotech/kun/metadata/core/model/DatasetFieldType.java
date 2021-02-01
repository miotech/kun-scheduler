package com.miotech.kun.metadata.core.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

public class DatasetFieldType implements Serializable {
    @JsonIgnore
    private static final long serialVersionUID = -1603335407509L;

    private static final Logger logger = LoggerFactory.getLogger(DatasetFieldType.class);

    private final Type type;
    private final String rawType;

    @JsonCreator
    public DatasetFieldType(@JsonProperty("type") Type type, @JsonProperty("rawType") String rawType) {
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
        BOOLEAN,
        JSON,
        UNKNOW;

        @JsonCreator
        public static DatasetFieldType.Type forValue(String value) {
            return valueOf(value);
        }

        @JsonValue
        public String toValue() {
            return this.name();
        }
    }

    public static Type convertRawType(String rawType) {
        if ("string".equals(rawType) || "STRING".equals(rawType) || rawType.startsWith("varchar") || rawType.startsWith("char")) {
            return Type.CHARACTER;
        } else if ("timestamp".equals(rawType) || "date".equals(rawType)) {
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
        } else if ("UNKNOW".equals(rawType)) {
            return Type.UNKNOW;
        } else {
            logger.warn("unknown type: {}", rawType);
            return Type.UNKNOW;
        }
    }
}
