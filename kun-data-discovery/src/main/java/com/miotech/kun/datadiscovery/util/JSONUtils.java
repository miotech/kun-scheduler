package com.miotech.kun.datadiscovery.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.miotech.kun.commons.utils.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JSONUtils {
    private JSONUtils() {
    }

    private static final Logger logger = LoggerFactory.getLogger(JSONUtils.class);
    private static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .setSerializationInclusion(JsonInclude.Include.NON_NULL);

    public static <T> String toJsonString(T obj, TypeReference<T> typeRef) {
        try {
            return objectMapper.writerFor(typeRef).writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            logger.error("Error occurs when converting object: {} to JSON string: ", obj, e);
            throw ExceptionUtils.wrapIfChecked(e);
        }
    }

    public static <T> String toJsonString(T obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            logger.error("Error occurs when converting object: {} to JSON string: ", obj, e);
            throw ExceptionUtils.wrapIfChecked(e);
        }
    }

    public static <T> T jsonToObject(String str, Class<T> valueType) {
        try {
            return objectMapper.readValue(str, valueType);
        } catch (JsonProcessingException e) {
            logger.error("Error occurs when converting JSON: {} to object: ", str, e);
            throw ExceptionUtils.wrapIfChecked(e);
        }
    }

    public static <T> T jsonToObject(String str, TypeReference<T> typeRef) {
        try {
            return objectMapper.readValue(str, typeRef);
        } catch (JsonProcessingException e) {
            logger.error("Error occurs when converting JSON: {} to object: ", str, e);
            throw ExceptionUtils.wrapIfChecked(e);
        }
    }

    public static <T> T jsonToObject(Object obj, Class<T> valueType) {
        try {
            String json = toJsonString(obj);
            return jsonToObject(json, valueType);
        } catch (Exception e) {
            logger.error("Error occurs when converting object: {} to object: ", obj, e);
            throw ExceptionUtils.wrapIfChecked(e);
        }
    }

    public static <T> List<T> jsonArrayToList(String str, Class<T> valueType) {
        try {
            return objectMapper.readValue(str, new TypeReference<List<T>>() {
            });
        } catch (JsonProcessingException e) {
            logger.error("Error occurs when converting JSON: {} to List: ", str, e);
            throw ExceptionUtils.wrapIfChecked(e);
        }
    }

    public static <T> T jsonToObject(InputStream inputStream, Class<T> valueType) {
        try {
            return objectMapper.readValue(inputStream, valueType);
        } catch (IOException e) {
            logger.error("Error occurs when convert inputStream to Object: ", e);
            throw ExceptionUtils.wrapIfChecked(e);
        }
    }

    public static <K, V> Map<K, V> jsonStringToMap(String jsonStr, Class<K> keyType, Class<V> valueType) {
        try {
            return objectMapper.readValue(
                    jsonStr,
                    objectMapper.getTypeFactory().constructMapType(HashMap.class, keyType, valueType)
            );
        } catch (JsonProcessingException e) {
            logger.error("Error occurs when convert jsonStr: {} to Map: ", jsonStr, e);
            throw ExceptionUtils.wrapIfChecked(e);
        }
    }

    public static <K, V> Map<K, V> objToMap(Object obj, Class<K> keyType, Class<V> valueType) {
        try {
            String jsonStr = toJsonString(obj);
            return objectMapper.readValue(
                    jsonStr,
                    objectMapper.getTypeFactory().constructMapType(HashMap.class, keyType, valueType)
            );
        } catch (JsonProcessingException e) {
            logger.error("Error occurs when convert object: {} to Map: ", obj, e);
            throw ExceptionUtils.wrapIfChecked(e);
        }
    }

    public static JsonNode stringToJson(String jsonStr) {
        try {
            return objectMapper.readTree(jsonStr);
        } catch (JsonProcessingException e) {
            logger.error("Error occurs when convert jsonStr: {} to JsonNode: ", jsonStr, e);
            throw ExceptionUtils.wrapIfChecked(e);
        }
    }

    public static <T> T JsonNodeToObject(JsonNode jsonNode, Class<T> valueType) {
        try {
            return objectMapper.treeToValue(jsonNode, valueType);
        } catch (JsonProcessingException e) {
            logger.error("Error occurs when convert jsonNode to Object: ", e);
            throw ExceptionUtils.wrapIfChecked(e);
        }
    }

    public static JsonNode objectToJsonNode(Object obj) {
        return objectMapper.convertValue(obj, JsonNode.class);
    }

}
