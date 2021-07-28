package com.miotech.kun.dataplatform.common.tasktemplate.renderer;

import com.google.common.base.Strings;
import com.miotech.kun.dataplatform.model.taskdefinition.TaskConfig;
import com.miotech.kun.dataplatform.model.taskdefinition.TaskDefinition;
import com.miotech.kun.dataplatform.model.tasktemplate.TaskTemplate;
import com.miotech.kun.workflow.utils.JSONUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

abstract public class SparkSubmitBasedTaskTemplateRender extends TaskTemplateRenderer{
    protected static final String SPARK_CONFIG_KEY = "sparkConf";
    protected static final String SPARK_SUBMIT_CMD = "command";
    protected static final String[] SPARK_SUBMIT_OPTIONS = {"master", "deploy-mode", "proxy-user", "class"};

    @Override
    public TaskConfig render(Map<String, Object> taskConfig, TaskTemplate taskTemplate, TaskDefinition taskDefinition) {
        String sparkSubmitCmd = buildSparkSubmitCmd(taskConfig, taskTemplate, taskDefinition);
        Map<String, Object> configMap = new HashMap<>();
        configMap.put(SPARK_SUBMIT_CMD, sparkSubmitCmd);

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

    public abstract String buildSparkSubmitCmd(Map<String, Object> taskConfig, TaskTemplate taskTemplate, TaskDefinition taskDefinition);

}
