package com.miotech.kun.workflow.utils;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

/**
 * A customized serializer which converts list of long fields to list of strings.
 */
public class JsonLongListFieldSerializer extends JsonSerializer<List<Long>> {
    @Override
    public void serialize(List<Long> value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartArray();
        for (Long longVal : value) {
            if (Objects.nonNull(longVal)) {
                gen.writeString(longVal.toString());
            } else {
                gen.writeNull();
            }
        }
        gen.writeEndArray();
    }
}
