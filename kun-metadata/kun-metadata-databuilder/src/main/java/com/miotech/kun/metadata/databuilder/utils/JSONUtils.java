package com.miotech.kun.metadata.databuilder.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.miotech.kun.commons.utils.ExceptionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JSONUtils {
    private JSONUtils() {
    }

    private static final ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .setSerializationInclusion(JsonInclude.Include.NON_NULL);

    public static <T> String toJsonString(T obj, TypeReference<T> typeRef) {
        try {
            return objectMapper.writerFor(typeRef).writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw ExceptionUtils.wrapIfChecked(e);
        }
    }

    public static <T> String toJsonString(T obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw ExceptionUtils.wrapIfChecked(e);
        }
    }

    public static <T> T jsonToObject(String str, Class<T> valueType) {
        try {
            return objectMapper.readValue(str, valueType);
        } catch (JsonProcessingException e) {
            throw ExceptionUtils.wrapIfChecked(e);
        }
    }

    public static <T> T jsonToObject(String str, TypeReference<T> typeRef) {
        try {
            return objectMapper.readValue(str, typeRef);
        } catch (JsonProcessingException e) {
            throw ExceptionUtils.wrapIfChecked(e);
        }
    }

    public static <T> List<T> jsonArrayToList(String str, Class<T> valueType) {
        try {
            return objectMapper.readValue(str, new TypeReference<List<T>>() {
            });
        } catch (JsonProcessingException e) {
            throw ExceptionUtils.wrapIfChecked(e);
        }
    }

    public static Map<String, Object> jsonStringToMap(String jsonStr) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(
                    jsonStr,
                    mapper.getTypeFactory().constructMapType(HashMap.class, String.class, Object.class)
            );
        } catch (JsonProcessingException e) {
            throw ExceptionUtils.wrapIfChecked(e);
        }
    }

    public static JsonNode stringToJson(String s) {
        try {
            return objectMapper.readTree(s);
        } catch (JsonProcessingException e) {
            throw ExceptionUtils.wrapIfChecked(e);
        }
    }

    public static <T> T JsonNodeToObject(JsonNode jsonNode, Class<T> valueType) {
        try {
            return objectMapper.treeToValue(jsonNode, valueType);
        } catch (JsonProcessingException e) {
            throw ExceptionUtils.wrapIfChecked(e);
        }
    }

    public static JsonNode objectToJsonNode(Object obj) {
        return objectMapper.convertValue(obj, JsonNode.class);
    }

}
