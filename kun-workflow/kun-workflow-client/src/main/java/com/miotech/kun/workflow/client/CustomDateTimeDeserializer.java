package com.miotech.kun.workflow.client;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.miotech.kun.commons.utils.DateTimeUtils;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

public class CustomDateTimeDeserializer extends JsonDeserializer<OffsetDateTime> {

    private DateTimeFormatter formatter;

    public CustomDateTimeDeserializer() {
        this(DateTimeUtils.ISO_DATETIME_NANO_DATETIME_FORMATTER);
    }

    public CustomDateTimeDeserializer(DateTimeFormatter formatter) {
        this.formatter = formatter;
    }

    @Override
    public OffsetDateTime deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        return OffsetDateTime.parse(parser.getText(), this.formatter);
    }
}