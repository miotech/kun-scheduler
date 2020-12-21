package com.miotech.kun.dataplatform.common.tasktemplate.renderer;

import com.miotech.kun.dataplatform.model.taskdefinition.TaskConfig;
import com.miotech.kun.dataplatform.model.taskdefinition.TaskDefinition;
import com.miotech.kun.dataplatform.model.tasktemplate.TaskTemplate;
import com.miotech.kun.workflow.client.model.ConfigKey;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class TaskTemplateRenderer {

    public Map<String, Object> buildTaskConfig(Map<String, Object> taskConfig, TaskTemplate taskTemplate, TaskDefinition taskDefinition) {
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
                    configMap.put(key, taskConfig.get(key));
                }
            }
        }

        return configMap;
    }

    public abstract TaskConfig render(Map<String, Object> taskConfig, TaskTemplate taskTemplate, TaskDefinition taskDefinition);
}
