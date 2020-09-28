package com.miotech.kun.metadata.databuilder.extract.tool;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.miotech.kun.metadata.databuilder.model.DatasetField;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

import java.util.List;
import java.util.stream.Collectors;

public class FieldFlatUtilTest {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    public void testFlatFields_simple() throws JsonProcessingException {
        String json = "{\"id\":1,\"name\":\"test\",\"hobbies\":[\"rubik\",\"cooking\"],\"married\":false}";
        JsonNode jsonNode = MAPPER.readTree(json);

        List<DatasetField> fields = FieldFlatUtil.flatFields(jsonNode, null);
        List<String> fieldStrs = datasetField2String(fields);

        MatcherAssert.assertThat(fieldStrs, Matchers.containsInAnyOrder("id-NUMBER-", "name-STRING-", "hobbies-ARRAY-", "married-BOOL-"));
    }

    @Test
    public void testFlatFields_unknown() throws JsonProcessingException {
        String json = "{\"id\":null,\"name\":\"test\",\"hobbies\":[\"rubik\",\"cooking\"],\"married\":false}";
        JsonNode jsonNode = MAPPER.readTree(json);

        List<DatasetField> fields = FieldFlatUtil.flatFields(jsonNode, null);
        List<String> fieldStrs = datasetField2String(fields);

        MatcherAssert.assertThat(fieldStrs, Matchers.containsInAnyOrder("id-UNKNOWN-", "name-STRING-", "hobbies-ARRAY-", "married-BOOL-"));
    }

    @Test
    public void testFlatFields_nested() throws JsonProcessingException {
        String json = "{\"id\":1,\"name\":\"test\",\"hobbies\":[{\"name\":\"rubik\"},{\"name\":\"cooking\"}],\"married\":false}";
        JsonNode jsonNode = MAPPER.readTree(json);

        List<DatasetField> fields = FieldFlatUtil.flatFields(jsonNode, null);
        List<String> fieldStrs = datasetField2String(fields);

        MatcherAssert.assertThat(fieldStrs, Matchers.containsInAnyOrder("id-NUMBER-", "name-STRING-", "hobbies.name-STRING-", "married-BOOL-"));
    }

    @Test
    public void testFlatFields_emptyArray() throws JsonProcessingException {
        String json = "{\"id\":1,\"name\":\"test\",\"hobbies\":[],\"married\":false}";
        JsonNode jsonNode = MAPPER.readTree(json);

        List<DatasetField> fields = FieldFlatUtil.flatFields(jsonNode, null);
        List<String> fieldStrs = datasetField2String(fields);

        MatcherAssert.assertThat(fieldStrs, Matchers.containsInAnyOrder("id-NUMBER-", "name-STRING-", "hobbies-ARRAY-", "married-BOOL-"));
    }

    private List<String> datasetField2String(List<DatasetField> fields) {
        return fields.stream().map(field -> {
            String name = field.getName();
            String rawType = field.getFieldType().getRawType();
            String comment = field.getComment();
            return name + "-" + rawType + "-" + comment;
        }).collect(Collectors.toList());
    }

}
