package com.miotech.kun.workflow.core.execution;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.util.Map;

public class ConfigSerializer extends JsonSerializer<Config> {

    @Override
    public void serialize(Config config, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        Map<String,Object> values = config.getValues();
        gen.writeStartObject();
        for(Map.Entry<String,Object> entry : values.entrySet() ){
            gen.writeObjectField(entry.getKey(),entry.getValue());
        }
        gen.writeEndObject();
    }
}
