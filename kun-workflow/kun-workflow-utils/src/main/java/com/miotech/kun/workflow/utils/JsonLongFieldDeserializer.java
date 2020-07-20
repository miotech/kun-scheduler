package com.miotech.kun.workflow.utils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;

/**
 * A customized deserializer which converts string fields to long.
 */
public class JsonLongFieldDeserializer extends JsonDeserializer<Long> {
    /**
     * @param jsonParser Parsed used for reading JSON content
     * @param deserializationContext Context that can be used to access information about this deserialization activity.
     * @return parsed long value
     * @throws IOException
     * @throws NumberFormatException if the string does not contain a parsable long value
     */
    @Override
    public Long deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        String value = jsonParser.getText();
        return value == null ? null : Long.parseLong(value);
    }
}
