package com.miotech.kun.workflow.client;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.miotech.kun.commons.utils.CustomDateTimeDeserializer;
import com.miotech.kun.commons.utils.ExceptionUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;


public class CustomDateTimeDeserializerTest {
    private ObjectMapper mapper;
    private CustomDateTimeDeserializer deserializer;

    @Before
    public void setup() {
        mapper = new ObjectMapper();
        deserializer = new CustomDateTimeDeserializer();
    }

    @Test
    public void testParseWithOffset_UTC() {
        String json = String.format("{\"value\":%s}", "\"2020-01-01T02:37:14.288Z\"");
        OffsetDateTime offsetDateTime = deserializeDateTime(json);
        assertThat(offsetDateTime.getYear(), is(2020) );
        assertThat(offsetDateTime.getMonthValue(), is(1) );
        assertThat(offsetDateTime.getDayOfMonth(), is(1) );
        assertThat(offsetDateTime.getHour(), is(2) );
        assertThat(offsetDateTime.getMinute(), is(37) );
        assertThat(offsetDateTime.getSecond(), is(14) );
        assertThat(offsetDateTime.getNano(), is(288000000) );
        assertThat(offsetDateTime.getOffset(), is(ZoneOffset.UTC) );
    }

    @Test
    public void testParseWithOffset_SHANGHAI() {
        String json = String.format("{\"value\":%s}", "\"2020-01-01T12:27:08.098+08:00\"");
        OffsetDateTime offsetDateTime = deserializeDateTime(json);
        assertThat(offsetDateTime.getYear(), is(2020) );
        assertThat(offsetDateTime.getMonthValue(), is(1) );
        assertThat(offsetDateTime.getDayOfMonth(), is(1) );
        assertThat(offsetDateTime.getHour(), is(12) );
        assertThat(offsetDateTime.getMinute(), is(27) );
        assertThat(offsetDateTime.getSecond(), is(8) );
        assertThat(offsetDateTime.getNano(), is(98000000) );
        assertThat(offsetDateTime.getOffset(), is(ZoneOffset.ofHoursMinutes(8, 0)) );
    }

    private OffsetDateTime deserializeDateTime(String json) {
        try {
            InputStream stream = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));
            JsonParser parser = mapper.getFactory().createParser(stream);
            DeserializationContext ctx = mapper.getDeserializationContext();
            parser.nextToken();
            parser.nextToken();
            parser.nextToken();
            return deserializer.deserialize(parser, ctx);
        } catch (IOException e) {
            throw ExceptionUtils.wrapIfChecked(e);
        }
    }
}