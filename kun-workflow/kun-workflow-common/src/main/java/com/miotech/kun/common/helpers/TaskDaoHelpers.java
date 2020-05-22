package com.miotech.kun.common.helpers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.miotech.kun.commons.utils.ExceptionUtils;
import com.miotech.kun.workflow.core.model.common.Param;
import com.miotech.kun.workflow.core.model.common.Variable;
import com.miotech.kun.workflow.core.model.task.ScheduleConf;
import com.miotech.kun.workflow.utils.JSONUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class TaskDaoHelpers {
    private static Logger logger = LoggerFactory.getLogger(TaskDaoHelpers.class);

    private TaskDaoHelpers() {}

    public static String variableDefsToJson(List<Variable> variableDefs) {
        return JSONUtils.toJsonString(variableDefs);
    }

    public static String argumentsToJson(List<Param> arguments) {
        return JSONUtils.toJsonString(arguments);
    }

    public static String scheduleConfToJson(ScheduleConf scheduleConf) {
        return JSONUtils.toJsonString(scheduleConf);
    }

    public static List<Variable> jsonStringToVariableDefs(String jsonStr) {
        try {
            return JSONUtils.jsonToObject(jsonStr, new TypeReference<List<Variable>>() {});
        } catch (JsonProcessingException e) {
            logger.error(e.getMessage());
            throw ExceptionUtils.wrapIfChecked(e);
        }
    }

    public static List<Param> jsonStringToArguments(String jsonStr) {
        try {
            return JSONUtils.jsonToObject(jsonStr, new TypeReference<List<Param>>() {});
        } catch (JsonProcessingException e) {
            logger.error(e.getMessage());
            throw ExceptionUtils.wrapIfChecked(e);
        }
    }

    public static ScheduleConf jsonStringToScheduleConf(String jsonStr)  {
        try {
            return JSONUtils.jsonToObject(jsonStr, new TypeReference<ScheduleConf>() {});
        } catch (JsonProcessingException e) {
            logger.error(e.getMessage());
            throw ExceptionUtils.wrapIfChecked(e);
        }
    }
}
