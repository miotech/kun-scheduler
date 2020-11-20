package com.miotech.kun.dataplatform.common.tasktemplate.renderer;

import com.miotech.kun.dataplatform.model.taskdefinition.TaskConfig;
import com.miotech.kun.dataplatform.model.tasktemplate.TaskTemplate;
import com.miotech.kun.workflow.client.model.ConfigKey;
import com.miotech.kun.workflow.utils.JSONUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class TaskTemplateRenderer {

    public Map<String, Object> buildTaskConfig(Map<String, Object> taskConfig, TaskTemplate taskTemplate) {
        Map<String, Object> defaultValues = taskTemplate.getDefaultValues();
        Map<String, Object> configMap = new HashMap<>();
        if (defaultValues != null && !defaultValues.isEmpty()) {
            configMap.putAll(defaultValues);
        }
        List<String> paramKeys = taskTemplate
                .getOperator()
                .getConfigDef()
                .stream()
                .map(ConfigKey::getName)
                .collect(Collectors.toList());
        if (taskConfig != null) {
            for (String key: taskConfig.keySet()) {
                if (paramKeys.contains(key)) {
                    Object value = taskConfig.get(key);
                    if (value instanceof Map) {
                        Map<String, String> valueMap = (Map<String, String>) value;
                        configMap.put(key, JSONUtils.toJsonString(valueMap));
                    } else {
                        configMap.put(key, value);
                    }

                }
            }
        }

        return configMap;
    }

    public abstract TaskConfig render(Map<String, Object> taskConfig, TaskTemplate taskTemplate);
}
