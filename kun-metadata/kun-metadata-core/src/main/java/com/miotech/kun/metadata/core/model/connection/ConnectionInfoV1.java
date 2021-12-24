package com.miotech.kun.metadata.core.model.connection;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;

import java.util.Map;

/**
 * just used to compatible old data
 * will be removed after discovery refactor
 */
public class ConnectionInfoV1 {

    private final Map<String, Object> values;

    @JsonCreator
    public ConnectionInfoV1(@JsonProperty("values") Map<String, Object> values) {
        Preconditions.checkArgument(values != null, "values should not be null");
        this.values = values;
    }

    public boolean contains(String name) {
        return values.containsKey(name);
    }

    public Map<String, Object> getValues() {
        return values;
    }

    @Override
    public String toString() {
        return "ConnectionInfo{" +
                "values=" + values +
                '}';
    }
}