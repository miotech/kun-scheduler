package com.miotech.kun.workflow.client.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.miotech.kun.workflow.core.execution.ConfigDef;

import java.io.IOException;

public class ConfigKey {

    private static final Object NO_DEFAULT = new Object();

    private String name;
    private ConfigDef.Type type;
    @JsonDeserialize(using = ConfigKeyDefaultValueDeSerializer.class)
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Object defaultValue;
    private boolean reconfigurable;
    private String documentation;
    private String displayName;

    public String getName() {
        return name;
    }

    public ConfigDef.Type getType() {
        return type;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(Object defaultValue) {
        this.defaultValue = defaultValue;
    }

    public boolean isReconfigurable() {
        return reconfigurable;
    }

    public void setReconfigurable(boolean reconfigurable) {
        this.reconfigurable = reconfigurable;
    }

    public String getDocumentation() {
        return documentation;
    }

    public void setDocumentation(String documentation) {
        this.documentation = documentation;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public static class ConfigKeyDefaultValueDeSerializer extends JsonDeserializer<Object> {

        @Override
        public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
            if (p.getObjectId() == null) {
                return NO_DEFAULT;
            } else {
                return p.getObjectId();
            }
        }
    }
}