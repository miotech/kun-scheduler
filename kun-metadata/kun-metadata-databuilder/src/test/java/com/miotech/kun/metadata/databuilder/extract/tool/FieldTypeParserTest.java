package com.miotech.kun.metadata.databuilder.extract.tool;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.miotech.kun.metadata.common.utils.FieldTypeParser;
import com.miotech.kun.metadata.core.model.dataset.DatasetField;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

public class FieldTypeParserTest {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    public void testFlatFields_simple() throws JsonProcessingException {
        String json = "{\"id\":1,\"name\":\"test\",\"hobbies\":[\"rubik\",\"cooking\"],\"married\":false}";
        JsonNode jsonNode = MAPPER.readTree(json);

        List<DatasetField> fields = FieldTypeParser.parse(jsonNode, null);
        List<String> fieldStrs = datasetField2String(fields);

        MatcherAssert.assertThat(fieldStrs, Matchers.containsInAnyOrder("id-NUMBER-", "name-CHARACTER-", "hobbies-ARRAY-", "married-BOOLEAN-"));
    }

    @Test
    public void testFlatFields_unknown() throws JsonProcessingException {
        String json = "{\"id\":null,\"name\":\"test\",\"hobbies\":[\"rubik\",\"cooking\"],\"married\":false}";
        JsonNode jsonNode = MAPPER.readTree(json);

        List<DatasetField> fields = FieldTypeParser.parse(jsonNode, null);
        List<String> fieldStrs = datasetField2String(fields);

        MatcherAssert.assertThat(fieldStrs, Matchers.containsInAnyOrder("id-UNKNOWN-", "name-CHARACTER-", "hobbies-ARRAY-", "married-BOOLEAN-"));
    }

    @Test
    public void testFlatFields_nested() throws JsonProcessingException {
        String json = "{\"id\":1,\"name\":\"test\",\"hobbies\":[{\"name\":\"rubik\"},{\"name\":\"cooking\"}],\"married\":false}";
        JsonNode jsonNode = MAPPER.readTree(json);

        List<DatasetField> fields = FieldTypeParser.parse(jsonNode, null);
        List<String> fieldStrs = datasetField2String(fields);

        MatcherAssert.assertThat(fieldStrs, Matchers.containsInAnyOrder("id-NUMBER-", "name-CHARACTER-", "hobbies.name-CHARACTER-", "married-BOOLEAN-"));
    }

    @Test
    public void testFlatFields_emptyArray() throws JsonProcessingException {
        String json = "{\"id\":1,\"name\":\"test\",\"hobbies\":[],\"married\":false}";
        JsonNode jsonNode = MAPPER.readTree(json);

        List<DatasetField> fields = FieldTypeParser.parse(jsonNode, null);
        List<String> fieldStrs = datasetField2String(fields);

        MatcherAssert.assertThat(fieldStrs, Matchers.containsInAnyOrder("id-NUMBER-", "name-CHARACTER-", "hobbies-ARRAY-", "married-BOOLEAN-"));
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
