package com.miotech.kun.dataplatform.common.tasktemplate.renderer;

import com.miotech.kun.dataplatform.model.taskdefinition.TaskConfig;
import com.miotech.kun.dataplatform.model.taskdefinition.TaskDefinition;
import com.miotech.kun.dataplatform.model.tasktemplate.TaskTemplate;
import com.miotech.kun.workflow.client.model.ConfigKey;
import com.miotech.kun.workflow.utils.JSONUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class TaskTemplateRenderer {

    public Map<String, Object> buildTaskConfig(Map<String, Object> taskConfig, TaskTemplate taskTemplate, TaskDefinition taskDefinition) {
        Map<String, Object> configMap = new HashMap<>();

        List<String> paramKeys = taskTemplate
                .getOperator()
                .getConfigDef()
                .stream()
                .map(ConfigKey::getName)
                .collect(Collectors.toList());

        Map<String, Object> defaultValues = taskTemplate.getDefaultValues();
        if (taskConfig == null) {
            configMap.putAll(defaultValues);
        } else {
            for (String key : paramKeys) {
                Object mergedValue = mergeDefaultAndUserDefinedConfig(defaultValues, taskConfig, key);
                if (mergedValue != null)
                    configMap.put(key, mergedValue);
            }
        }

        return configMap;
    }

    public Object mergeDefaultAndUserDefinedConfig(Map<String, Object> defaultValues, Map<String, Object> userDefinedConfig, String key) {

        Object userDefinedValue = userDefinedConfig.get(key);
        if (userDefinedValue == null) {
            return defaultValues.get(key);
        }
        if (userDefinedValue instanceof Map) {
            Object defaultValue = defaultValues.get(key);
            Map<String, String> defaultKV = null;
            if (defaultValue instanceof String) {
                defaultKV = JSONUtils.jsonStringToStringMap((String) defaultValue);
            } else if (defaultValue instanceof Map) {
                defaultKV = (Map<String, String>) defaultValue;
            } else {
                throw new IllegalStateException(String.format("unexpected param type for key: %s", key));
            }
            defaultKV.putAll((Map<String, String>) userDefinedValue);
            return JSONUtils.toJsonString(defaultKV);
        } else {
            return userDefinedValue;
        }
    }

    public abstract TaskConfig render(Map<String, Object> taskConfig, TaskTemplate taskTemplate, TaskDefinition taskDefinition);
}
