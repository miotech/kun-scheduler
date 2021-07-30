package com.miotech.kun.dataplatform.common.tasktemplate.renderer;

import com.google.common.base.Strings;
import com.miotech.kun.dataplatform.model.taskdefinition.TaskConfig;
import com.miotech.kun.dataplatform.model.taskdefinition.TaskDefinition;
import com.miotech.kun.dataplatform.model.tasktemplate.TaskTemplate;
import com.miotech.kun.workflow.client.model.ConfigKey;
import com.miotech.kun.workflow.utils.JSONUtils;

import java.util.*;
import java.util.stream.Collectors;

import static com.miotech.kun.workflow.operator.SparkConfiguration.SPARK_YARN_HOST;

abstract public class SparkSubmitBasedTaskTemplateRender extends TaskTemplateRenderer{
    protected static final String SPARK_CONFIG_KEY = "sparkConf";
    protected static final String SPARK_SUBMIT_CMD = "command";
    protected static final String SPARK_MASTER = "master";
    protected static final String SPARK_DEPLOY_MODE = "deployMode";
    protected static final String SPARK_YARN_HOST = "yarnHost";
    protected static final String[] SPARK_SUBMIT_OPTIONS = {"master", "deploy-mode", "proxy-user", "class"};

    @Override
    public TaskConfig render(Map<String, Object> taskConfig, TaskTemplate taskTemplate, TaskDefinition taskDefinition) {
        Map<String, Object> mergedConfig = mergeDefaultConfAndUserDefinedConf(taskConfig, taskTemplate);
        String sparkSubmitCmd = buildSparkSubmitCmd(mergedConfig, taskTemplate, taskDefinition);
        Map<String, Object> configMap = new HashMap<>();
        configMap.put(SPARK_SUBMIT_CMD, sparkSubmitCmd);

        String master = (String) mergedConfig.get(SPARK_MASTER);
        if(!Strings.isNullOrEmpty(master)){
            configMap.put(SPARK_MASTER, master);
        }

        String deployMode = (String) mergedConfig.get(SPARK_DEPLOY_MODE);
        if(!Strings.isNullOrEmpty(deployMode)){
            configMap.put(SPARK_DEPLOY_MODE, deployMode);
        }

        String yarnHost = (String) mergedConfig.get(SPARK_YARN_HOST);
        if(!Strings.isNullOrEmpty(yarnHost)){
            configMap.put(SPARK_YARN_HOST, yarnHost);
        }

        return  TaskConfig.newBuilder()
                .withParams(configMap)
                .build();
    }

    List<String> buildBasicSparkCmd(Map<String, Object> taskConfig){
        List<String> params = new ArrayList<>();

        // parse spark-submit options
        for(String key : SPARK_SUBMIT_OPTIONS){
            String value = (String) taskConfig.get(key);
            if(!Strings.isNullOrEmpty(value)){
                params.add("--" + key + " " + value);
            }
        }

        //parse spark conf
        Map<String, String> sparkConfig;
        if (taskConfig.get(SPARK_CONFIG_KEY) instanceof String) {
            sparkConfig = JSONUtils.jsonStringToStringMap((String) taskConfig.get(SPARK_CONFIG_KEY));
        } else {
            sparkConfig = (Map<String, String>) taskConfig.get(SPARK_CONFIG_KEY);
        }
        if(sparkConfig != null){
            for(Map.Entry<String, String> entry: sparkConfig.entrySet()){
                params.add("--conf " + entry.getKey() + "=" + entry.getValue());
            }
        }
        return params;
    }

    public Map<String, Object> mergeDefaultConfAndUserDefinedConf(Map<String, Object> taskConfig, TaskTemplate taskTemplate){
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

            for (String key : paramKeys) {
                Object mergedValue = mergeDefaultAndUserDefinedConfig(defaultValues, taskConfig, key);
                if (mergedValue != null)
                    configMap.put(key, mergedValue);
            }
        }
        return configMap;
    }

    public abstract String buildSparkSubmitCmd(Map<String, Object> taskConfig, TaskTemplate taskTemplate, TaskDefinition taskDefinition);

}
