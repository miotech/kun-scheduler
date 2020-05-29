package com.miotech.kun.metadata.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.miotech.kun.metadata.extract.tool.FieldFlatUtil;
import com.miotech.kun.metadata.model.DatasetField;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

import java.util.List;

public class FieldFlatUtilTest {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    public void testFlatFields_simple() throws JsonProcessingException {
        String json = "{\"id\":1,\"name\":\"unden\",\"hobbies\":[\"rubik\",\"cooking\"],\"married\":false}";
        JsonNode jsonNode = MAPPER.readTree(json);

        List<DatasetField> fields = FieldFlatUtil.flatFields(jsonNode, null);
        MatcherAssert.assertThat(fields, Matchers.containsInAnyOrder(new DatasetField("id", "NUMBER", ""),
                new DatasetField("name", "STRING", ""),
                new DatasetField("hobbies", "ARRAY", ""),
                new DatasetField("married", "BOOL", "")));
    }

    @Test
    public void testFlatFields_nested() throws JsonProcessingException {
        String json = "{\"id\":1,\"name\":\"unden\",\"hobbies\":[{\"name\":\"rubik\"},{\"name\":\"cooking\"}],\"married\":false}";
        JsonNode jsonNode = MAPPER.readTree(json);

        List<DatasetField> fields = FieldFlatUtil.flatFields(jsonNode, null);
        MatcherAssert.assertThat(fields, Matchers.containsInAnyOrder(new DatasetField("id", "NUMBER", ""),
                new DatasetField("name", "STRING", ""),
                new DatasetField("hobbies.name", "STRING", ""),
                new DatasetField("married", "BOOL", "")));
    }

    @Test
    public void testFlatFields_emptyArray() throws JsonProcessingException {
        String json = "{\"id\":1,\"name\":\"unden\",\"hobbies\":[],\"married\":false}";
        JsonNode jsonNode = MAPPER.readTree(json);

        List<DatasetField> fields = FieldFlatUtil.flatFields(jsonNode, null);
        MatcherAssert.assertThat(fields, Matchers.containsInAnyOrder(new DatasetField("id", "NUMBER", ""),
                new DatasetField("name", "STRING", ""),
                new DatasetField("hobbies", "ARRAY", ""),
                new DatasetField("married", "BOOL", "")));
    }

}
