package com.miotech.kun.common.helpers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.miotech.kun.commons.utils.ExceptionUtils;
import com.miotech.kun.workflow.core.model.common.Param;
import com.miotech.kun.workflow.utils.JSONUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class OperatorDaoHelpers {
    private static final Logger logger = LoggerFactory.getLogger(OperatorDaoHelpers.class);

    public static List<Param> jsonStringToParams(String jsonStr) {
        try {
            return JSONUtils.jsonToObject(jsonStr, new TypeReference<List<Param>>() {});
        } catch (JsonProcessingException e) {
            logger.error(e.getMessage());
            throw ExceptionUtils.wrapIfChecked(e);
        }
    }

    public static String paramsToJsonString(List<Param> params) {
        return JSONUtils.toJsonString(params);
    }
}
