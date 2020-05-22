package com.miotech.kun.common.task.dao;

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
        return JSONUtils.jsonToObject(jsonStr, new TypeReference<List<Variable>>() {});
    }

    public static List<Param> jsonStringToArguments(String jsonStr) {
        return JSONUtils.jsonToObject(jsonStr, new TypeReference<List<Param>>() {});
    }

    public static ScheduleConf jsonStringToScheduleConf(String jsonStr)  {
        return JSONUtils.jsonToObject(jsonStr, new TypeReference<ScheduleConf>() {});
    }
}
