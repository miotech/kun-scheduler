package com.miotech.kun.workflow.core.execution;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.collect.ImmutableMap;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;

@JsonSerialize(using = Config.ConfigSerializer.class)
@JsonDeserialize(using = Config.ConfigDeserializer.class)
public class Config {
    public static final Config EMPTY = new Config(ImmutableMap.of());

    private final Map<String, Object> values;

    public Config(Map<String, Object> values) {
        validateValues(values);
        this.values = ImmutableMap.copyOf(values);
    }

    public Config(ConfigDef definition, Map<String, Object> originals) {
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

    public Integer getInt(String name) {
        return (Integer) values.get(name);
    }

    public Integer getInt(String name, int defaultValue) {
        return (Integer) values.getOrDefault(name, defaultValue);
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

    /**
     * Override current {@link Config} by another {@link Config} to generate a combined {@link Config}.
     * @param another another {@link Config}
     * @return
     */
    public Config overrideBy(Config another) {
        Map<String, Object> newValues = new HashMap<>(values);
        Map<String, Object> anotherValues = another.getValues();
        for (Map.Entry<String, Object> e : anotherValues.entrySet()) {
            newValues.put(e.getKey(), e.getValue());
        }
        return new Config(newValues);
    }

    /**
     * Validate this {@link Config} using {@link ConfigDef}.
     * @param def {@link ConfigDef} needs to be checked against to.
     */
    public void validateBy(ConfigDef def) {
        def.validate(values);
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

    public static class ConfigSerializer extends JsonSerializer<Config> {
        @Override
        public void serialize(Config config, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeObject(config.getValues());
        }
    }

    public static class ConfigDeserializer extends JsonDeserializer<Config> {
        private static final TypeReference<HashMap<String,Object>> typeRef = new TypeReference<HashMap<String, Object>>() {};

        @Override
        public Config deserialize(JsonParser parser, DeserializationContext ctxt) throws IOException {
            // deserialize values from a plain object
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> values = mapper.readValue(parser, typeRef);
            return new Config(values);
        }
    }
}
