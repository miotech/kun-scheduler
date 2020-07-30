package com.miotech.kun.dataplatform.common.tasktemplate.renderer;

import com.miotech.kun.dataplatform.model.taskdefinition.TaskConfig;
import com.miotech.kun.dataplatform.model.tasktemplate.TaskTemplate;
import com.miotech.kun.workflow.client.model.ConfigKey;
import com.miotech.kun.workflow.utils.JSONUtils;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class SparkConfigTaskTemplateRender extends TaskTemplateRenderer {

    private static final String KUN_DATA_PLATFORM_CONFIG_PREFIX = "kun.dataplatform.";
    private static final String SPARK_CONFIG_KEY = "sparkConf";

    @Override
    public TaskConfig render(Map<String, Object> taskConfig, TaskTemplate taskTemplate) {
        Map<String, Object> configMap = buildTaskConfig(taskConfig, taskTemplate);

        return  TaskConfig.newBuilder()
                .withParams(configMap)
                .build();
    }

    private Map<String, Object> buildSparkConfig(Map<String, Object> taskConfig, TaskTemplate taskTemplate) {
        List<String> paramKeys = taskTemplate
                .getOperator()
                .getConfigDef()
                .stream()
                .map(ConfigKey::getName)
                .collect(Collectors.toList());
        Map<String, Object> config = new HashMap<>();
        if (taskConfig.containsKey(SPARK_CONFIG_KEY) &&
                paramKeys.contains(SPARK_CONFIG_KEY)) {
            Map<String, String> sparkConfig = JSONUtils.jsonStringToStringMap((String) taskConfig.get(SPARK_CONFIG_KEY));

            // add extra parameter into sparkConf
            for (String key: taskConfig.keySet()) {
                if (!paramKeys.contains(key)) {
                    sparkConfig.put(KUN_DATA_PLATFORM_CONFIG_PREFIX + key, taskConfig.get(key).toString());
                } else if (!key.equals(SPARK_CONFIG_KEY)){
                    config.put(key, taskConfig.get(key));
                }
            }
            config.put(SPARK_CONFIG_KEY, JSONUtils.toJsonString(sparkConfig));
        } else {
            config.putAll(taskConfig);
        }
        return config;
    }
}
