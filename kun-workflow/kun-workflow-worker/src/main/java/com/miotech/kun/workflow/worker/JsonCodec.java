package com.miotech.kun.workflow.worker;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonCodec {
    public static final ObjectMapper MAPPER;

    static {
        MAPPER = new ObjectMapper();
    }
}

