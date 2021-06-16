package com.miotech.kun.workflow.client;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.miotech.kun.commons.utils.CustomDateTimeSerializer;
import com.miotech.kun.commons.utils.ExceptionUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class CustomDateTimeSerializerTest {
    private CustomDateTimeSerializer serializer;
    private JsonGenerator jsonGenerator;
    private SerializerProvider serializerProvider;
    private Writer jsonWriter;

    @Before
    public void setup() {
        try {
            jsonWriter = new StringWriter();
            jsonGenerator = new JsonFactory().createGenerator(jsonWriter);
            serializerProvider = new ObjectMapper().getSerializerProvider();
            serializer = new CustomDateTimeSerializer();
        } catch (IOException e) {
            throw ExceptionUtils.wrapIfChecked(e);
        }
    }

    @Test
    public void testParseWithOffset_UTC() throws IOException {
        OffsetDateTime date = OffsetDateTime.of(
                2020,
                1,
                1,
                2,
                37,
                14,
                288000000,
                ZoneOffset.UTC
        );
        serializer.serialize(date, jsonGenerator, serializerProvider);
        jsonGenerator.flush();
        assertThat(jsonWriter.toString(), is("\"2020-01-01T02:37:14.288Z\""));
    }

    @Test
    public void testParseWithOffset_SHANGHAI() throws IOException {
        OffsetDateTime date = OffsetDateTime.of(
                2020,
                1,
                1,
                2,
                37,
                14,
                288000000,
                ZoneOffset.ofHours(8)
        );
        serializer.serialize(date, jsonGenerator, serializerProvider);
        jsonGenerator.flush();
        assertThat(jsonWriter.toString(), is("\"2020-01-01T02:37:14.288+08:00\""));
    }
}