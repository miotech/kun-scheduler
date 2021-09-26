package com.miotech.kun.dataplatform.web.common.tasktemplate.renderer;

import com.google.common.base.Strings;
import com.miotech.kun.dataplatform.facade.model.taskdefinition.TaskConfig;
import com.miotech.kun.dataplatform.facade.model.taskdefinition.TaskDefinition;
import com.miotech.kun.dataplatform.web.model.tasktemplate.TaskTemplate;
import com.miotech.kun.workflow.client.model.ConfigKey;
import com.miotech.kun.workflow.utils.JSONUtils;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

import static com.miotech.kun.workflow.operator.SparkConfiguration.*;

@Component
public class SparkSubmitBasedTaskTemplateRender extends TaskTemplateRenderer {
    protected static final String SPARK_CONFIG_KEY = "sparkConf";
    protected static final String SPARK_YARN_HOST = "yarnHost";
    protected static final String[] SPARK_SUBMIT_OPTIONS = {SPARK_ENTRY_CLASS};

    @Override
    public TaskConfig render(Map<String, Object> taskConfig, TaskTemplate taskTemplate, TaskDefinition taskDefinition) {
        Map<String, Object> mergedConfig = mergeDefaultConfAndUserDefinedConf(taskConfig, taskTemplate);
        Map<String, Object> configMap = new HashMap<>();

        // fill sparkSubmitOperator conf
        Map<String, String> sparkConf = getSparkConf(mergedConfig);
        configMap.put(SPARK_CONF, JSONUtils.toJsonString(sparkConf));

        Map<String, String> sparkSubmitParams = getSparkSubmitParams(mergedConfig, taskDefinition);
        configMap.put(SPARK_SUBMIT_PARMAS, JSONUtils.toJsonString(sparkSubmitParams));

        String yarnHost = (String) mergedConfig.get(SPARK_YARN_HOST);
        if (!Strings.isNullOrEmpty(yarnHost)) {
            configMap.put(SPARK_YARN_HOST, yarnHost);
        }

        String application = (String) mergedConfig.get(SPARK_APPLICATION);
        configMap.put(SPARK_APPLICATION, application);

        String applicationArgs = (String) mergedConfig.get(SPARK_APPLICATION_ARGS);
        if (!Strings.isNullOrEmpty(applicationArgs)) {
            configMap.put(SPARK_APPLICATION_ARGS, applicationArgs);
        }


        return TaskConfig.newBuilder()
                .withParams(configMap)
                .build();
    }

    public Map<String, String> getSparkSubmitParams(Map<String, Object> taskConfig, TaskDefinition taskDefinition) {
        Map<String, String> params = new HashMap<>();
        // parse spark-submit options
        for (String key : SPARK_SUBMIT_OPTIONS) {
            String value = (String) taskConfig.get(key);
            if (!Strings.isNullOrEmpty(value)) {
                params.put(key, value);
            }
        }

        // set spark app name
        params.put("name", taskDefinition.getName());
        return params;
    }

    public Map<String, String> getSparkConf(Map<String, Object> taskConfig) {

        //parse spark conf
        Map<String, String> sparkConfig;
        if (taskConfig.get(SPARK_CONFIG_KEY) instanceof String) {
            sparkConfig = JSONUtils.jsonStringToStringMap((String) taskConfig.get(SPARK_CONFIG_KEY));
        } else {
            sparkConfig = (Map<String, String>) taskConfig.get(SPARK_CONFIG_KEY);
        }

        if (sparkConfig == null) {
            sparkConfig = new HashMap<>();
        }

        return sparkConfig;
    }

    public Map<String, Object> mergeDefaultConfAndUserDefinedConf(Map<String, Object> taskConfig, TaskTemplate taskTemplate) {
        Map<String, Object> configMap = new HashMap<>();

        Map<String, Object> defaultValues = taskTemplate.getDefaultValues();
        if (taskConfig == null) {
            configMap.putAll(defaultValues);
        } else {
            Set<String> paramKeys = taskTemplate
                    .getOperator()
                    .getConfigDef()
                    .stream()
                    .map(ConfigKey::getName)
                    .collect(Collectors.toSet());
            paramKeys.addAll(taskConfig.keySet());
            paramKeys.addAll(defaultValues.keySet());

            for (String key : paramKeys) {
                Object mergedValue = mergeDefaultAndUserDefinedConfig(defaultValues, taskConfig, key);
                if (mergedValue != null)
                    configMap.put(key, mergedValue);
            }
        }
        return configMap;
    }

}
