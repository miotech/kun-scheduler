package com.miotech.kun.dataplatform.common.tasktemplate.renderer;

import com.miotech.kun.dataplatform.model.taskdefinition.TaskConfig;
import com.miotech.kun.dataplatform.model.taskdefinition.TaskDefinition;
import com.miotech.kun.dataplatform.model.tasktemplate.TaskTemplate;
import com.miotech.kun.workflow.client.model.ConfigKey;
import com.miotech.kun.workflow.utils.JSONUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class SparkSubmitTaskTemplateRender extends TaskTemplateRenderer {

    private static final String KUN_DATA_PLATFORM_CONFIG_PREFIX = "kun.dataplatform";
    private static final String KUN_DATA_PLATFORM_DATASOURCE_PREFIX = "kun.dataplatform.datasource";
    private static final String SPARK_CONFIG_KEY = "sparkConf";
    private static final String SPARK_SUBMIT_CMD = "sparkSubmitCmd";
    private static final String SPARK_JAVA_OPTIONS_KEY = "spark.driver.extraJavaOptions";

    @Override
    public TaskConfig render(Map<String, Object> taskConfig, TaskTemplate taskTemplate, TaskDefinition taskDefinition) {
        String sparkSubmitCmd = buildSparkSubmitCmd(taskConfig, taskTemplate, taskDefinition);
        Map<String, Object> configMap = new HashMap<>();
        configMap.put(SPARK_SUBMIT_CMD, sparkSubmitCmd);

        return  TaskConfig.newBuilder()
                .withParams(configMap)
                .build();
    }

    public String buildSparkSubmitCmd(Map<String, Object> taskConfig, TaskTemplate taskTemplate, TaskDefinition taskDefinition) {
        Map<String, Object> configMap = super.buildTaskConfig(taskConfig, taskTemplate, taskDefinition);
        Map<String, String> sparkConfig;

        if (configMap.get(SPARK_CONFIG_KEY) instanceof String) {
            sparkConfig = JSONUtils.jsonStringToStringMap((String) configMap.get(SPARK_CONFIG_KEY));
        } else {
            sparkConfig = (Map<String, String>) configMap.get(SPARK_CONFIG_KEY);
        }

        String sparkSubmitCmd = buildSparkConfStringFromMap(sparkConfig, (String) configMap.get("application"), (String) configMap.get("args"));
        return sparkSubmitCmd;
    }

    public String buildSparkConfStringFromMap(Map<String, String> sparkConfig, String appMain, String appArgs){
        List<String> params = new ArrayList<>();
        params.add("spark-submit");
        for(Map.Entry<String, String> entry: sparkConfig.entrySet()){
            params.add("--conf " + entry.getKey() + "=" + entry.getValue());
        }
        params.add(appMain);
        params.add(appArgs);
        return String.join(" ", params);
    }

}
