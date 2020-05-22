package com.miotech.kun.workflow.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.miotech.kun.commons.utils.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

public class JSONUtils {

    private static final Logger logger = LoggerFactory.getLogger(JSONUtils.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static String toJsonString(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            logger.error("Error in json to string: ", e);
            throw ExceptionUtils.wrapIfChecked(e);
        }
    }

    public static <T> T jsonToObject(String str, Class<T> valueType )
            throws JsonProcessingException {
        return objectMapper.readValue(str, valueType);
    }

    public static <T> T jsonToObject(InputStream inputStream, Class<T> valueType )
            throws IOException {
        return objectMapper.readValue(inputStream, valueType);
    }
}
