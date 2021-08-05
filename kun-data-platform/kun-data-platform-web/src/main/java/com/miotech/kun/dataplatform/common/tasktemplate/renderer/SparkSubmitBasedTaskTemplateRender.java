package com.miotech.kun.dataplatform.common.tasktemplate.renderer;

import com.google.common.base.Strings;
import com.miotech.kun.dataplatform.model.taskdefinition.TaskConfig;
import com.miotech.kun.dataplatform.model.taskdefinition.TaskDefinition;
import com.miotech.kun.dataplatform.model.tasktemplate.TaskTemplate;
import com.miotech.kun.workflow.client.model.ConfigKey;
import com.miotech.kun.workflow.utils.JSONUtils;

import java.util.*;
import java.util.stream.Collectors;

import static com.miotech.kun.workflow.operator.SparkConfiguration.*;

abstract public class SparkSubmitBasedTaskTemplateRender extends TaskTemplateRenderer {
    protected static final String SPARK_CONFIG_KEY = "sparkConf";
    protected static final String SPARK_MASTER = "master";
    protected static final String SPARK_DEPLOY_MODE = "deployMode";
    protected static final String SPARK_YARN_HOST = "yarnHost";
    protected static final String[] SPARK_SUBMIT_OPTIONS = {"master", "deploy-mode", "proxy-user", "class"};

    @Override
    public TaskConfig render(Map<String, Object> taskConfig, TaskTemplate taskTemplate, TaskDefinition taskDefinition) {
        Map<String, Object> mergedConfig = mergeDefaultConfAndUserDefinedConf(taskConfig, taskTemplate);
        String sparkSubmitParams = buildSparkSubmitCmd(mergedConfig, taskTemplate, taskDefinition);
        Map<String, Object> configMap = new HashMap<>();
        configMap.put(SPARK_BASE_COMMAND, getSparkBaseCmd());
        configMap.put(SPARK_SUBMIT_PARMAS, sparkSubmitParams);

        String master = (String) mergedConfig.get(SPARK_MASTER);
        if (!Strings.isNullOrEmpty(master)) {
            configMap.put(SPARK_MASTER, master);
        }

        String deployMode = (String) mergedConfig.get("deploy-mode");
        if (!Strings.isNullOrEmpty(deployMode)) {
            configMap.put(SPARK_DEPLOY_MODE, deployMode);
        }

        String yarnHost = (String) mergedConfig.get(SPARK_YARN_HOST);
        if (!Strings.isNullOrEmpty(yarnHost)) {
            configMap.put(SPARK_YARN_HOST, yarnHost);
        }

        //s3 credential
        configMap.put(VAR_S3_ACCESS_KEY, CONF_S3_ACCESS_KEY_VALUE_DEFAULT);
        configMap.put(VAR_S3_SECRET_KEY, CONF_S3_SECRET_KEY_VALUE_DEFAULT);

        return TaskConfig.newBuilder()
                .withParams(configMap)
                .build();
    }

    List<String> buildBasicSparkCmd(Map<String, Object> taskConfig, TaskDefinition taskDefinition) {
        List<String> params = new ArrayList<>();

        // parse spark-submit options
        for (String key : SPARK_SUBMIT_OPTIONS) {
            String value = (String) taskConfig.get(key);
            if (!Strings.isNullOrEmpty(value)) {
                params.add("--" + key + " " + value);
            }
        }

        // set spark app name
        params.add("--name" + " " + taskDefinition.getName());

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
        // lineage conf
        String jars;
        if (sparkConfig.containsKey("spark.jars")) {
            jars = sparkConfig.get("spark.jars") + "," + CONF_LINEAGE_JAR_PATH_VALUE_DEFAULT;
        } else {
            jars = CONF_LINEAGE_JAR_PATH_VALUE_DEFAULT;
        }

        //TODO: if lineage jar not configed, adding "spark.sql.queryExecutionListeners" in sparkConf will throw exception
        //TODO: Solution: add this config to operator level
        sparkConfig.put("spark.jars", jars);
        sparkConfig.put("spark.sql.queryExecutionListeners", "za.co.absa.spline.harvester.listener.SplineQueryExecutionListener");
        sparkConfig.put("spark.hadoop.spline.hdfs_dispatcher.address", CONF_LINEAGE_OUTPUT_PATH_VALUE_DEFAULT);
        sparkConfig.put("spark.fs.s3a.access.key", CONF_S3_ACCESS_KEY_VALUE_DEFAULT);
        sparkConfig.put("spark.fs.s3a.secret.key", CONF_S3_SECRET_KEY_VALUE_DEFAULT);

        for (Map.Entry<String, String> entry : sparkConfig.entrySet()) {
            params.add("--conf " + entry.getKey() + "=" + entry.getValue());
        }

        return params;
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

            for (String key : paramKeys) {
                Object mergedValue = mergeDefaultAndUserDefinedConfig(defaultValues, taskConfig, key);
                if (mergedValue != null)
                    configMap.put(key, mergedValue);
            }
        }
        return configMap;
    }

    public abstract String buildSparkSubmitCmd(Map<String, Object> taskConfig, TaskTemplate taskTemplate, TaskDefinition taskDefinition);
    public abstract String getSparkBaseCmd();

}
