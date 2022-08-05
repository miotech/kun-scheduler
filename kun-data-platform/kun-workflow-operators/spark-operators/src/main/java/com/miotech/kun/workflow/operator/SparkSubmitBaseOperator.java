package com.miotech.kun.workflow.operator;

import com.google.common.base.Strings;
import com.miotech.kun.workflow.core.execution.*;
import com.miotech.kun.workflow.utils.JSONUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.miotech.kun.commons.utils.HdfsFileSystem;
import com.miotech.kun.metadata.core.model.dataset.DataStore;
import com.miotech.kun.metadata.core.model.dataset.DataStoreType;
import org.apache.hadoop.conf.Configuration;

import java.io.IOException;

import java.util.*;

import static com.miotech.kun.workflow.operator.SparkConfiguration.*;

abstract public class SparkSubmitBaseOperator extends KunOperator {

    private static final Logger logger = LoggerFactory.getLogger(SparkSubmitBaseOperator.class);
    private SparkOperatorUtils sparkOperatorUtils = new SparkOperatorUtils();
    private final String WAREHOUSE_URL = "warehouseUrl";//todo read from config file


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
        String taskRunTick = context.getScheduleTime();
        sparkConf.put("spark.hadoop.taskRun.scheduledTick", taskRunTick);
        String taskTarget = (String) context.getExecuteTarget().getProperty("schema");
        sparkConf.put("spark.hadoop.taskRun.target", taskTarget);
        if (!sparkConf.containsKey("spark.yarn.queue")) {
            String queueName = context.getQueueName();
            sparkConf.put("spark.yarn.queue", queueName);
        }

        // lineage conf
        String configLineageOutputPath = SparkConfiguration.getString(context, CONF_LINEAGE_OUTPUT_PATH);
        String configLineageJarPath = SparkConfiguration.getString(context, CONF_LINEAGE_JAR_PATH);
        String configS3AccessKey = SparkConfiguration.getString(context, CONF_S3_ACCESS_KEY);
        String configS3SecretKey = SparkConfiguration.getString(context, CONF_S3_SECRET_KEY);
        String configDataLakePackages = SparkConfiguration.getString(context, SPARK_DATA_LAKE_PACKAGES);
        String sparkSqlExtensions = SparkConfiguration.getString(context, SPARK_SQL_EXTENSIONS);
        String sparkSerializer = SparkConfiguration.getString(context, SPARK_SERIALIZER);

        List<String> jars = new ArrayList<>();
        if (sparkConf.containsKey("spark.jars")) {
            jars.addAll(Arrays.asList(sparkConf.get("spark.jars").split(",")));
        }
        if (!Strings.isNullOrEmpty(configLineageJarPath)) {
            jars.add(configLineageJarPath);
            sparkConf.put("spark.sql.queryExecutionListeners", "za.co.absa.spline.harvester.listener.SplineQueryExecutionListener");
        }
        if (!Strings.isNullOrEmpty(configDataLakePackages)) {
            jars.addAll(Arrays.asList(configDataLakePackages.split(",")));
        }
        if (!Strings.isNullOrEmpty(sparkSqlExtensions)) {
            sparkConf.put(SPARK_SQL_EXTENSIONS, sparkSqlExtensions);
        }
        if (!Strings.isNullOrEmpty(sparkSerializer)) {
            sparkConf.put(SPARK_SERIALIZER, sparkSerializer);
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
        Map<String, String> defaultSparkConf = new HashMap<>();

        try {
            String defaultSparkConfStr = config.getString(KUN_SPARK_CONF);
            defaultSparkConf = JSONUtils.jsonStringToStringMap(Strings.isNullOrEmpty(defaultSparkConfStr) ? "{}" : defaultSparkConfStr);
        } catch (Exception e) {
            logger.error("ignore illegal default spark conf", e);
        }
        logger.debug("default spark conf is {}",defaultSparkConf);


        Map<String, String> taskConf = JSONUtils.jsonStringToStringMap(Strings.isNullOrEmpty(sparkConfStr) ? "{}" : sparkConfStr);

        Map<String, String> sparkConf = overrideSparkConfByTask(defaultSparkConf, taskConf);

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
                logger.debug("going to analysis lineage...");
                TaskAttemptReport taskAttemptReport = SparkQueryPlanLineageAnalyzer.lineageAnalysis(context.getConfig(), taskRunId);
                if (taskAttemptReport != null) {
                    report(taskAttemptReport);
                }
                logger.debug("point output dataset to validated version if necessary");
                pointToLatestValidatedVersion(taskAttemptReport.getOutlets(), context.getConfig());
            } catch (Exception e) {
                logger.error("Failed after finish: {}", e);
            }
        }
        return finalStatus;
    }

    private void pointToLatestValidatedVersion(List<DataStore> outputs, Config config) throws Exception {
        HdfsFileSystem hdfsFileSystem = initFileSystem(config);
        for (DataStore output : outputs) {
            if (!output.getType().equals(DataStoreType.HIVE_TABLE)) {
                continue;
            }
            String database = output.getDatabaseName();
            String table = output.getName();
            logger.debug("going to get latest version for database : {}, table :{}", database, table);
            String latestVersion = getLatestVersion(hdfsFileSystem, database, table);
            if (latestVersion != null) {
                logger.debug("database: {} table : {} latest version is {}", database, table);
                pointToValidatedVersion(hdfsFileSystem, database, table, latestVersion);
            }
        }
    }

    private HdfsFileSystem initFileSystem(Config config) throws Exception {
        logger.debug("config is {}", config);
        Configuration conf = new Configuration();

        String configS3AccessKey = config.getString(CONF_S3_ACCESS_KEY);
        if (!Strings.isNullOrEmpty(configS3AccessKey)) {
            conf.set("fs.s3a.access.key", configS3AccessKey);
        }
        String configS3SecretKey = config.getString(CONF_S3_SECRET_KEY);
        if (!Strings.isNullOrEmpty(configS3SecretKey)) {
            conf.set("fs.s3a.secret.key", configS3SecretKey);
        }

        conf.set("fs.hdfs.impl", org.apache.hadoop.hdfs.DistributedFileSystem.class.getName());
        conf.set("fs.file.impl", org.apache.hadoop.fs.LocalFileSystem.class.getName());

        return new HdfsFileSystem(WAREHOUSE_URL, conf);
    }

    //read latest validated commit from .hooide
    private String getLatestVersion(HdfsFileSystem hdfsFileSystem, String database, String table) throws IOException {
        String path = "/" + database + "/" + table + "/.hoodie";
        logger.debug("getting latest version from path :{} ", path);
        List<String> files = hdfsFileSystem.getFilesInDir(path);
        String latestVersion = null;
        for (String fileName : files) {
            String suffix = fileName.substring(fileName.lastIndexOf(".") + 1);
            if (suffix.equals("commit")) {
                String version = getVersion(fileName);
                if (latestVersion == null || version.compareTo(latestVersion) > 0) {
                    latestVersion = version;
                }
            }
        }
        return latestVersion;
    }

    private String getVersion(String file) {
        logger.debug("commit  file is : {}", file);
        return file.substring(0, file.lastIndexOf("."));
    }

    private void pointToValidatedVersion(HdfsFileSystem hdfsFileSystem, String database, String table, String version) throws IOException {
        String path = "/" + database + "/" + table + "/.hoodie";
        String latest = version + ".commit";
        String validating = version + ".validating";
        logger.debug("rename {} to {}", latest, validating);
        hdfsFileSystem.renameFiles(path + "/" + latest, path + "/" + validating);
    }

    private Map<String, String> overrideSparkConfByTask(Map<String, String> defaultConf, Map<String, String> taskConf) {
        Map<String, String> runTimeConf = new HashMap<>(defaultConf);
        for (Map.Entry<String, String> entry : taskConf.entrySet()) {
            runTimeConf.put(entry.getKey(), entry.getValue());
        }
        return runTimeConf;
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
                .define(CONF_S3_SECRET_KEY, ConfigDef.Type.STRING, CONF_S3_SECRET_KEY_VALUE_DEFAULT, true, "if using s3 to store lineage analysis report, need s3 credentials", CONF_S3_SECRET_KEY)
                .define(KUN_SPARK_CONF, ConfigDef.Type.STRING, DEFAULT_KUN_SPARK_CONF, true, "default key-value spark conf", KUN_SPARK_CONF);
    }

    @Override
    public Resolver getResolver() {
        // TODO: implement this
        return new NopResolver();
    }


}
