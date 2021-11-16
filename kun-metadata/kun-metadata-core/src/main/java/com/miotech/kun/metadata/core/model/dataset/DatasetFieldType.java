package com.miotech.kun.metadata.core.model.dataset;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

import java.io.Serializable;
import java.util.Objects;

public class DatasetFieldType implements Serializable {
    @JsonIgnore
    private static final long serialVersionUID = -1603335407509L;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DatasetFieldType that = (DatasetFieldType) o;
        return type == that.type && Objects.equals(rawType, that.rawType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, rawType);
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
        UNKNOWN;

        @JsonCreator
        public static DatasetFieldType.Type forValue(String value) {
            return valueOf(value);
        }

        @JsonValue
        public String toValue() {
            return this.name();
        }
    }

}
