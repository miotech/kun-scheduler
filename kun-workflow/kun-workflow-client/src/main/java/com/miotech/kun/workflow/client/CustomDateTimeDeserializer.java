package com.miotech.kun.workflow.client;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

public class CustomDateTimeDeserializer extends JsonDeserializer<OffsetDateTime> {

    private DateTimeFormatter formatter;

    public CustomDateTimeDeserializer() {
        this(
              new DateTimeFormatterBuilder()
                        // date/time
                        .appendPattern("yyyy-MM-dd'T'HH:mm:ss.SSS")
                        // offset
                        .appendPattern("XXX")
                        // create formatter
                        .toFormatter()
        );
    }

    public CustomDateTimeDeserializer(DateTimeFormatter formatter) {
        this.formatter = formatter;
    }

    @Override
    public OffsetDateTime deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        return OffsetDateTime.parse(parser.getText(), this.formatter);
    }
}