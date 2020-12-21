package com.miotech.kun.dataplatform.common.tasktemplate.renderer;

import com.miotech.kun.commons.utils.IdGenerator;
import com.miotech.kun.dataplatform.model.taskdefinition.TaskConfig;
import com.miotech.kun.dataplatform.model.taskdefinition.TaskDefinition;
import com.miotech.kun.dataplatform.model.tasktemplate.ParameterDefinition;
import com.miotech.kun.dataplatform.model.tasktemplate.TaskTemplate;
import com.miotech.kun.workflow.client.model.ConfigKey;
import com.miotech.kun.workflow.operator.SparkConfiguration;
import com.miotech.kun.workflow.utils.JSONUtils;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class SparkConfigTaskTemplateRender extends TaskTemplateRenderer {

    private static final String KUN_DATA_PLATFORM_CONFIG_PREFIX = "kun.dataplatform";
    private static final String KUN_DATA_PLATFORM_DATASOURCE_PREFIX = "kun.dataplatform.datasource";
    private static final String SPARK_CONFIG_KEY = "sparkConf";
    private static final String SPARK_JAVA_OPTIONS_KEY = "spark.driver.extraJavaOptions";

    @Override
    public TaskConfig render(Map<String, Object> taskConfig, TaskTemplate taskTemplate, TaskDefinition taskDefinition) {
        Map<String, Object> configMap = buildTaskConfig(taskConfig, taskTemplate, taskDefinition);

        return  TaskConfig.newBuilder()
                .withParams(configMap)
                .build();
    }

    @Override
    public Map<String, Object> buildTaskConfig(Map<String, Object> taskConfig, TaskTemplate taskTemplate, TaskDefinition taskDefinition) {
        Map<String, Object> configMap = super.buildTaskConfig(taskConfig, taskTemplate, taskDefinition);
        for (String key: taskConfig.keySet()) {
            if (!configMap.containsKey(key)) {
                configMap.put(key, taskConfig.get(key));
            }
        }
        List<String> paramKeys = taskTemplate
                .getOperator()
                .getConfigDef()
                .stream()
                .map(ConfigKey::getName)
                .collect(Collectors.toList());

        Map<String, ParameterDefinition> displayParams = taskTemplate
                .getDisplayParameters()
                .stream()
                .collect(Collectors.toMap(ParameterDefinition::getName, Function.identity()));
        Map<String, Object> config = new HashMap<>(configMap);
        if (configMap.containsKey(SPARK_CONFIG_KEY) &&
                paramKeys.contains(SPARK_CONFIG_KEY)) {
            Map<String, String> sparkConfig;
            if (configMap.get(SPARK_CONFIG_KEY) instanceof String) {
                sparkConfig = JSONUtils.jsonStringToStringMap((String) configMap.get(SPARK_CONFIG_KEY));
            } else {
                sparkConfig = (Map<String, String>) configMap.get(SPARK_CONFIG_KEY);
            }

            // add extra parameter into sparkConf as extraJavaOptions
            StringBuilder extraJavaOptions = new StringBuilder(sparkConfig.getOrDefault(SPARK_JAVA_OPTIONS_KEY, ""));

            Map<String, Object> extraConfig = new HashMap<>();
            for (String key: configMap.keySet()) {
                if (!paramKeys.contains(key)) {
                    extraConfig.put(key, configMap.get(key));
                }
                ParameterDefinition parameterDefinition = displayParams.get(key);
                // add datasource as variable
                if (parameterDefinition != null
                        && parameterDefinition.getType().equals("datasource")) {
                    extraJavaOptions.append(String.format(" -D%s.%s=${dataplatform.datasource.%s}", KUN_DATA_PLATFORM_DATASOURCE_PREFIX, configMap.get(key), configMap.get(key)));
                }
            }
            if (!extraConfig.isEmpty()) {
                String encodedString = Base64.getEncoder().encodeToString(JSONUtils.toJsonString(extraConfig).getBytes());
                extraJavaOptions.append(String.format(" -D%s=%s", KUN_DATA_PLATFORM_CONFIG_PREFIX, encodedString));
            }

            sparkConfig.put(SPARK_JAVA_OPTIONS_KEY, extraJavaOptions.toString());
            config.put(SPARK_CONFIG_KEY, JSONUtils.toJsonString(sparkConfig));
        } else {
            config.putAll(configMap);
        }
        config.put(SparkConfiguration.CONF_LIVY_BATCH_NAME, taskDefinition.getName() + " - " + IdGenerator.getInstance().nextId());
        return config;
    }
}
