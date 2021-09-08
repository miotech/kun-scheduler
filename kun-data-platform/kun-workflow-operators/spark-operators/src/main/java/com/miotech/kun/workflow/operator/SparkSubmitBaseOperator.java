package com.miotech.kun.workflow.operator;

import com.google.common.base.Strings;
import com.miotech.kun.workflow.core.execution.*;
import com.miotech.kun.workflow.utils.JSONUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static com.miotech.kun.workflow.operator.SparkConfiguration.*;

abstract public class SparkSubmitBaseOperator extends KunOperator {

    private static final Logger logger = LoggerFactory.getLogger(SparkSubmitBaseOperator.class);
    private SparkOperatorUtils sparkOperatorUtils = new SparkOperatorUtils();


    public abstract List<String> buildCmd(Map<String, String> sparkSubmitParams, Map<String, String> sparkConf, String app, String appArgs);

    public void addRunTimeParams(Map<String, String> sparkSubmitParams, OperatorContext context, Map<String, String> sparkConf) {
        String proxyuser = context.getConfig().getString(SPARK_PROXY_USER);
        if (!Strings.isNullOrEmpty(proxyuser)) {
            sparkSubmitParams.put(SPARK_PROXY_USER, proxyuser);
        }

        if (!sparkConf.containsKey("spark.submit.deployMode")) {
            sparkSubmitParams.put(SPARK_DEPLOY_MODE, "cluster");
        }
        if (!sparkConf.containsKey("spark.master")) {
            sparkSubmitParams.put(SPARK_MASTER, "yarn");
        }
    }

    public void addRunTimeSparkConfs(Map<String, String> sparkConf, OperatorContext context) {

        Long taskRunId = context.getTaskRunId();
        sparkConf.put("spark.hadoop.taskRunId", taskRunId.toString());
        // lineage conf
        String configLineageOutputPath = SparkConfiguration.getString(context, CONF_LINEAGE_OUTPUT_PATH);
        String configLineageJarPath = SparkConfiguration.getString(context, CONF_LINEAGE_JAR_PATH);
        String configS3AccessKey = SparkConfiguration.getString(context, CONF_S3_ACCESS_KEY);
        String configS3SecretKey = SparkConfiguration.getString(context, CONF_S3_SECRET_KEY);

        List<String> jars = new ArrayList<>();
        if (sparkConf.containsKey("spark.jars")) {
            jars.addAll(Arrays.asList(sparkConf.get("spark.jars").split(",")));
        }
        if (!Strings.isNullOrEmpty(configLineageJarPath)) {
            jars.add(configLineageJarPath);
            sparkConf.put("spark.sql.queryExecutionListeners", "za.co.absa.spline.harvester.listener.SplineQueryExecutionListener");
        }
        if (!jars.isEmpty()) {
            sparkConf.put("spark.jars", String.join(",", jars));
        }

        if (!Strings.isNullOrEmpty(configLineageOutputPath)) {
            sparkConf.put("spark.hadoop.spline.hdfs_dispatcher.address", configLineageOutputPath);
        }
        if (!Strings.isNullOrEmpty(configS3AccessKey)) {
            sparkConf.put("spark.fs.s3a.access.key", configS3AccessKey);
        }
        if (!Strings.isNullOrEmpty(configS3SecretKey)) {
            sparkConf.put("spark.fs.s3a.secret.key", configS3SecretKey);
        }

        if (!sparkConf.containsKey("spark.driver.memory")) {
            sparkConf.put("spark.driver.memory", "2g");
        }
    }

    @Override
    public void init() {
        OperatorContext context = getContext();
        logger.info("Recieved task config: {}", JSONUtils.toJsonString(context.getConfig()));

        sparkOperatorUtils.init(context, logger);
    }


    @Override
    public boolean run() {
        OperatorContext context = getContext();
        Config config = context.getConfig();
        Long taskRunId = context.getTaskRunId();

        String sparkParamsStr = config.getString(SPARK_SUBMIT_PARMAS);
        Map<String, String> sparkSubmitParams = JSONUtils.jsonStringToStringMap(Strings.isNullOrEmpty(sparkParamsStr) ? "{}" : sparkParamsStr);
        String sparkConfStr = config.getString(SPARK_CONF);
        Map<String, String> sparkConf = JSONUtils.jsonStringToStringMap(Strings.isNullOrEmpty(sparkConfStr) ? "{}" : sparkConfStr);

        // add run time configs
        addRunTimeSparkConfs(sparkConf, context);
        addRunTimeParams(sparkSubmitParams, context, sparkConf);

        //build shell cmd
        List<String> cmd = buildCmd(sparkSubmitParams, sparkConf, config.getString(SPARK_APPLICATION), config.getString(SPARK_APPLICATION_ARGS));
        logger.info("execute cmd: " + String.join(" ", cmd));

        boolean finalStatus = sparkOperatorUtils.execSparkSubmitCmd(cmd);
        if (finalStatus) {
            try {
                SparkOperatorUtils.waitForSeconds(10);
                TaskAttemptReport taskAttemptReport = SparkQueryPlanLineageAnalyzer.lineageAnalysis(context.getConfig(), taskRunId);
                if (taskAttemptReport != null)
                    report(taskAttemptReport);
            } catch (Exception e) {
                logger.error("Failed to parse lineage: {}", e);
            }
        }
        return finalStatus;
    }

    @Override
    public void abort() {
        sparkOperatorUtils.abort();
    }

    @Override
    public ConfigDef config() {
        return new ConfigDef()
                .define(SPARK_SUBMIT_PARMAS, ConfigDef.Type.STRING, "", true, "spark-submit parmas", SPARK_SUBMIT_PARMAS)
                .define(SPARK_CONF, ConfigDef.Type.STRING, "", true, "key-value spark conf", SPARK_CONF)
                .define(SPARK_PROXY_USER, ConfigDef.Type.STRING, SPARK_PROXY_USER_DEFAULT_VALUE, true, "proxy user", SPARK_PROXY_USER)
                .define(SPARK_APPLICATION, ConfigDef.Type.STRING, "", true, "entry jar/py file", SPARK_APPLICATION)
                .define(SPARK_APPLICATION_ARGS, ConfigDef.Type.STRING, "", true, "application arguments", SPARK_APPLICATION_ARGS)
                .define(SPARK_YARN_HOST, ConfigDef.Type.STRING, "", true, "Yarn host to submit application, in the format `ip:port`", SPARK_YARN_HOST)
                .define(CONF_LINEAGE_OUTPUT_PATH, ConfigDef.Type.STRING, CONF_LINEAGE_OUTPUT_PATH_VALUE_DEFAULT, true, "file system address to store lineage analysis report, in the format `s3a://BUCKET/path` or `hdfs://host:port/path`", CONF_LINEAGE_OUTPUT_PATH)
                .define(CONF_LINEAGE_JAR_PATH, ConfigDef.Type.STRING, CONF_LINEAGE_JAR_PATH_VALUE_DEFAULT, true, "the jar used for lineage analysis, in the format `s3a://BUCKET/xxx/xxx.jar` or `hdfs://host:port/xxx/xxx.jar`", CONF_LINEAGE_JAR_PATH)
                .define(CONF_S3_ACCESS_KEY, ConfigDef.Type.STRING, CONF_S3_ACCESS_KEY_VALUE_DEFAULT, true, "if using s3 to store lineage analysis report, need s3 credentials", CONF_S3_ACCESS_KEY)
                .define(CONF_S3_SECRET_KEY, ConfigDef.Type.STRING, CONF_S3_SECRET_KEY_VALUE_DEFAULT, true, "if using s3 to store lineage analysis report, need s3 credentials", CONF_S3_SECRET_KEY);
    }

    @Override
    public Resolver getResolver() {
        // TODO: implement this
        return new NopResolver();
    }


}
