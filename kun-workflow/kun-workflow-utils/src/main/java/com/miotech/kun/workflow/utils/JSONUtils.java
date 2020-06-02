package com.miotech.kun.workflow.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.miotech.kun.commons.utils.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class JSONUtils {
    private JSONUtils() { }

    private static final Logger logger = LoggerFactory.getLogger(JSONUtils.class);

    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addSerializer(Long.class, ToStringSerializer.instance);
        simpleModule.addSerializer(Long.TYPE, ToStringSerializer.instance);
        objectMapper.registerModule(simpleModule);
    }

    public static <T> String toJsonString(T obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            logger.error("Error occurs when converting object to JSON string: ", e);
            throw ExceptionUtils.wrapIfChecked(e);
        }
    }

    public static <T> T jsonToObject(String str, Class<T> valueType) {
        try {
            return objectMapper.readValue(str, valueType);
        } catch (JsonProcessingException e) {
            logger.error("Error occurs when converting JSON to object: ", e);
            throw ExceptionUtils.wrapIfChecked(e);
        }
    }

    public static <T> T jsonToObject(String str, TypeReference<T> typeRef) {
        try {
            return objectMapper.readValue(str, typeRef);
        } catch (JsonProcessingException e) {
            logger.error("Error occurs when converting JSON to object: ", e);
            throw ExceptionUtils.wrapIfChecked(e);
        }
    }

    public static <T> T jsonToObject(InputStream inputStream, Class<T> valueType )
            throws IOException {
        return objectMapper.readValue(inputStream, valueType);
    }

    public static Map<String, Object> jsonStringToMap(String jsonStr) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(
                    jsonStr,
                    mapper.getTypeFactory().constructMapType(HashMap.class, String.class, Object.class)
            );
        } catch (JsonProcessingException e) {
            logger.error(e.getMessage());
            throw ExceptionUtils.wrapIfChecked(e);
        }
    }

    public static Map<String, String> jsonStringToStringMap(String jsonStr) {
        ObjectMapper mapper = new ObjectMapper();
        logger.info("jsonStr = " + jsonStr);
        try {
            return mapper.readValue(
                    jsonStr,
                    mapper.getTypeFactory().constructMapType(HashMap.class, String.class, String.class)
            );
        } catch (JsonProcessingException e) {
            logger.error(e.getMessage());
            throw ExceptionUtils.wrapIfChecked(e);
        }
    }
}
