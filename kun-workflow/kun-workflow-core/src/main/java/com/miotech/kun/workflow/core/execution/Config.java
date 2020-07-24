package com.miotech.kun.workflow.core.execution;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableMap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;

public class Config {
    public static final Config EMPTY = new Config(ImmutableMap.of());

    private final Map<String, Object> values;

    @JsonCreator
    public Config(@JsonProperty("values") Map<String, Object> values) {
        validateValues(values);
        this.values = ImmutableMap.copyOf(values);
    }

    public Config(ConfigDef definition, Map<String, String> originals) {
        this(definition.parse(originals));
    }

    public int size() {
        return values.size();
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

    public Long getLong(String name) {
        return (Long) values.get(name);
    }

    public Long getLong(String name, long defaultValue) {
        return (Long) values.getOrDefault(name, defaultValue);
    }

    public String getString(String name) {
        return (String) values.get(name);
    }

    public String getString(String name, String defaultValue) {
        return (String) values.getOrDefault(name, defaultValue);
    }

    @SuppressWarnings("unchecked")
    public List<String> getList(String name) {
        return (List<String>) values.get(name);
    }

    @SuppressWarnings("unchecked")
    public List<String> getList(String name, List<String> defaultValue) {
        return (List<String>) values.getOrDefault(name, defaultValue);
    }

    public Config overrideBy(Config another) {
        Map<String, Object> newValues = new HashMap<>(values);
        Map<String, Object> anotherValues = another.getValues();
        for (Map.Entry<String, Object> e : anotherValues.entrySet()) {
            newValues.put(e.getKey(), e.getValue());
        }
        return new Config(newValues);
    }

    private void validateValues(Map<String, Object> values) {
        values.forEach(this::validateValue);
    }

    private void validateValue(String name, Object value) {
        if (!ConfigDef.Type.isCompatibleValue(value)) {
            throw new IllegalArgumentException(format("Incompatible value %s of config key %s", value, name));
        }
    }

    public static Config.Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {
        private final Map<String, Object> values;

        public Builder() {
            values = new HashMap<>();
        }

        public Builder addConfig(String name, Object value) {
            values.put(name, value);
            return this;
        }

        public Config build() {
            return new Config(values);
        }
    }
}
