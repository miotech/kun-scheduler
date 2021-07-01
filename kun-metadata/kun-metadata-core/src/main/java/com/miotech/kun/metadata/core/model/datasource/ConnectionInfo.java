package com.miotech.kun.metadata.core.model.datasource;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.Map;

import static java.lang.String.format;

public class ConnectionInfo {

    private static final List<Class> DEFAULT_VALUE_TYPES = Lists.newArrayList(String.class, Boolean.class, Integer.class);
    private final Map<String, Object> values;

    @JsonCreator
    public ConnectionInfo(@JsonProperty("values") Map<String, Object> values) {
        Preconditions.checkArgument(values != null, "values should not be null");
        validateValues(values);
        this.values = ImmutableMap.copyOf(values);
    }

    public boolean contains(String name) {
        return values.containsKey(name);
    }

    public Map<String, Object> getValues() {
        return values;
    }

    public Boolean getBoolean(String name) {
        return (Boolean) values.get(name);
    }

    public Boolean getBoolean(String name, boolean defaultValue) {
        return (Boolean) values.getOrDefault(name, defaultValue);
    }

    public Integer getInt(String name) {
        return (Integer) values.get(name);
    }

    public Integer getInt(String name, int defaultValue) {
        return (Integer) values.getOrDefault(name, defaultValue);
    }

    public String getString(String name) {
        return (String) values.get(name);
    }

    public String getString(String name, String defaultValue) {
        return (String) values.getOrDefault(name, defaultValue);
    }

    @Override
    public String toString() {
        return "ConnectionInfo{" +
                "values=" + values +
                '}';
    }

    /**
     * @param values Verify the value type of values, only support String, Integer, Boolean
     */
    private void validateValues(Map<String, Object> values) {
        for (Map.Entry<String, Object> entry : values.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (!isCompatible(value)) {
                throw new IllegalArgumentException(format("Incompatible value %s of config key %s", value, key));
            }
        }
    }

    private boolean isCompatible(Object value) {
        for (Class defaultValueType : DEFAULT_VALUE_TYPES) {
            if (defaultValueType.isAssignableFrom(value.getClass())) {
                return true;
            }
        }

        return false;
    }

}
