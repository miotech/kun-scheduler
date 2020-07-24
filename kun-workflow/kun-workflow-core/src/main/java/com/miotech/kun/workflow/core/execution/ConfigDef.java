package com.miotech.kun.workflow.core.execution;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

import static java.lang.String.format;

public class ConfigDef {
    private static final Pattern COMMA_WITH_WHITESPACE = Pattern.compile("\\s*,\\s*");

    private static final Object NO_VALUE = new Object();

    private final Map<String, ConfigKey> configKeys;

    public ConfigDef() {
        this.configKeys = new LinkedHashMap<>();
    }

    public ConfigDef define(ConfigKey key) {
        if (configKeys.containsKey(key.getName())) {
            throw new IllegalArgumentException("ConfigKey " + key.getName() + " is already defined.");
        }
        configKeys.put(key.getName(), key);
        return this;
    }

    public ConfigDef define(String name, Type type, Object defaultValue, boolean reconfigurable, String documentation, String displayName) {
        return define(new ConfigKey(name, type, defaultValue, reconfigurable, documentation, displayName));
    }

    public ConfigDef define(String name, Type type, boolean reconfigurable, String documentation, String displayName) {
        return define(new ConfigKey(name, type, ConfigKey.NO_DEFAULT, reconfigurable, documentation, displayName));
    }

    public Map<String, Object> parse(Map<String, String> props) {
        Map<String, Object> values = new HashMap<>();
        for (ConfigKey key : configKeys.values()) {
            String keyName = key.getName();
            Object value = parseValue(keyName, props.get(keyName), props.containsKey(keyName));
            if (value != NO_VALUE) {
                values.put(keyName, value);
            }
        }
        return values;
    }

    public boolean contains(String name) {
        return configKeys.containsKey(name);
    }

    public ConfigKey get(String name) {
        if (!configKeys.containsKey(name)) {
            throw new IllegalArgumentException("Unknown configuration '" + name + "'");
        }
        return configKeys.get(name);
    }

    public Collection<ConfigKey> configKeys() {
        return configKeys.values();
    }

    /**
     * Json serialization method
     * @return
     */
    @JsonValue
    public Collection<ConfigKey> toJson() {
        return configKeys.values();
    }

    private Object parseValue(String name, String value, boolean isSet) {
        ConfigKey key = get(name);
        if (isSet) {
            return parseValueOfType(value, key.getType());
        } else {
            return key.hasDefault() ? key.getDefaultValue() : NO_VALUE;
        }
    }

    private static Object parseValueOfType(String value, Type type) {
        String trimmed = value.trim();
        switch (type) {
            case BOOLEAN:
                if (trimmed.equalsIgnoreCase("true")) {
                    return Boolean.TRUE;
                } else if (trimmed.equalsIgnoreCase("false")) {
                    return Boolean.FALSE;
                } else {
                    throw new IllegalArgumentException("Expected value to be either true or false");
                }
            case STRING:
                return trimmed;
            case LONG:
                return Long.valueOf(trimmed);
            case LIST:
                if (trimmed.isEmpty()) {
                    return Collections.emptyList();
                } else {
                    return Arrays.asList(COMMA_WITH_WHITESPACE.split(trimmed, -1));
                }
            default:
                throw new UnsupportedOperationException("Unknown type '" + type + "'");
        }
    }

    public enum Type {
        BOOLEAN(Boolean.class),
        STRING(String.class),
        LONG(Long.class),
        LIST(List.class);

        private final Class<?> javaType;

        Type(Class<?> javaType) {
            this.javaType = javaType;
        }

        public static boolean isCompatibleValue(Object obj) {
            for (Type type : Type.values()) {
                if (type.isCompatible(obj)) {
                    return true;
                }
            }
            return false;
        }

        public boolean isCompatible(Object obj) {
            return javaType.isAssignableFrom(obj.getClass());
        }
    }

    public static class ConfigKey {
        private static final Object NO_DEFAULT = new Object();

        private final String name;
        private final Type type;
        @JsonSerialize(using = ConfigKeyDefaultValueSerializer.class)
        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        private final Object defaultValue;
        private final boolean reconfigurable;
        private final String documentation;
        private final String displayName;

        public ConfigKey(String name, Type type, Object defaultValue, boolean reconfigurable, String documentation, String displayName) {
            if (defaultValue != NO_DEFAULT && !type.isCompatible(defaultValue)) {
                throw new IllegalArgumentException(format("default value '%s' is not compatible with type '%s'", defaultValue, type));
            }
            this.name = name;
            this.type = type;
            this.defaultValue = defaultValue;
            this.reconfigurable = reconfigurable;
            this.documentation = documentation;
            this.displayName = displayName;
        }

        public boolean isRequired() {
            return !hasDefault();
        }

        public boolean hasDefault() {
            return !NO_DEFAULT.equals(defaultValue);
        }

        public String getName() {
            return name;
        }

        public Type getType() {
            return type;
        }

        public Object getDefaultValue() {
            return defaultValue;
        }

        public boolean isReconfigurable() {
            return reconfigurable;
        }

        public String getDocumentation() {
            return documentation;
        }

        public String getDisplayName() {
            return displayName;
        }

        public static class ConfigKeyDefaultValueSerializer extends JsonSerializer<Object> {
            @Override
            public boolean isEmpty(SerializerProvider provider, Object value) {
                return value == NO_DEFAULT;
            }

            @Override
            public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
                if (value != NO_DEFAULT) {
                    gen.writeObject(value);
                } else {
                    gen.writeNull();
                }
            }
        }
    }
}
