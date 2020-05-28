package com.miotech.kun.metadata.client;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.inject.Singleton;

@Singleton
public class ObjectMapperClient {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public <T> T convertValue(Object fromValue, Class<T> toValueType) {
        return MAPPER.convertValue(fromValue, toValueType);
    }

}
