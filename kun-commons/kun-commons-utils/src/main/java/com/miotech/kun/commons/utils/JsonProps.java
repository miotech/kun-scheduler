package com.miotech.kun.commons.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.javaprop.JavaPropsMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

public class JsonProps implements PropsProvider {

    private final Logger logger = LoggerFactory.getLogger(JsonProps.class);
    private final ObjectMapper objectMapper;
    private final JsonNode jsonNode;

    public JsonProps(ObjectMapper objectMapper, JsonNode jsonNode) {
        this.objectMapper = objectMapper;
        this.jsonNode = jsonNode;
    }


    @Override
    public <T> T getValue(String key, Class<T> valueType) {
        T value = null;
        JsonNode filedNode = getJsonNode(key);
        if (filedNode != null) {
            value = objectMapper.convertValue(filedNode, valueType);
        }
        return value;
    }

    @Override
    public <T> List<T> getValueList(String key, Class<T> valueType) {
        JsonNode filedNode = getJsonNode(key);
        try {
            return objectMapper.readerForListOf(valueType).readValue(filedNode);
        } catch (IOException e) {
            logger.error("Error occurs when converting JSON: {} to List: ", filedNode, e);
            throw ExceptionUtils.wrapIfChecked(e);
        }
    }

    @Override
    public boolean containsKey(String key) {
        JsonNode filedNode = getJsonNode(key);
        return filedNode != null ? true : false;
    }

    @Override
    public Properties toProperties() {
        JavaPropsMapper mapper = new JavaPropsMapper();
        try {
            return mapper.writeValueAsProperties(jsonNode);
        }catch (IOException e){
            logger.error("Error occurs when converting JSON: {} to properties: ", e);
            throw ExceptionUtils.wrapIfChecked(e);
        }
    }

    private JsonNode getJsonNode(String key) {
        String[] filedPaths = key.split("\\.");
        JsonNode filedNode = null;
        try {
            JsonNode root = jsonNode;
            for (String filed : filedPaths) {
                filedNode = root.get(filed);
                root = filedNode;
            }
        } catch (Exception e) {
            logger.warn("can not get key = " + key + "from jsonProps", e);
        }
        return filedNode;
    }

    @Override
    public String toString() {
        return "JsonProps{" +
                "jsonNode=" + jsonNode +
                '}';
    }
}
