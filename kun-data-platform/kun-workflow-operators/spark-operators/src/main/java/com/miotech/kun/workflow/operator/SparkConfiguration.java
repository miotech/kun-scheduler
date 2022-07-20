package com.miotech.kun.workflow.operator;

import com.miotech.kun.workflow.core.execution.OperatorContext;
import com.miotech.kun.workflow.utils.JSONUtils;

import java.util.Map;

public class SparkConfiguration {

    public static final String CONF_LIVY_HOST = "livyHost";

    public static final String CONF_LIVY_YARN_QUEUE = "queue";
    public static final String CONF_LIVY_YARN_QUEUE_DEFAULT = "default";

    public static final String CONF_LIVY_PROXY_USER = "proxyUser";
    public static final String CONF_LIVY_PROXY_DEFAULT = "hadoop";

    // BATCH JOB
    public static final String CONF_LIVY_BATCH_FILES = "files";
    public static final String CONF_LIVY_BATCH_JARS = "jars";
    public static final String CONF_LIVY_BATCH_APPLICATION = "application";
    public static final String CONF_LIVY_BATCH_CONF = "sparkConf";
    public static final String CONF_LIVY_BATCH_NAME = "sparkJobName";

    //task
    public static final String CONF_TASK_RUN_ID = "taskRunId";
    public static final String CONF_TASK_NAME = "taskName";

    // dispatch
    public static final String CONF_LIVY_DISPATCH_ADDRESS = "dispatchAddress";
    public static final String CONF_LIVY_DISPATCH_CONFIG = "dispatchConfig";

    //hdfs
    public static final String CONF_HDFS_URL = "hdfsUrl";

    // SQL
    public static final String CONF_LIVY_SHARED_SESSION = "sharedSession";
    public static final String CONF_LIVY_SHARED_SESSION_NAME = "sharedSessionName";

    public static final String CONF_SPARK_SQL = "sparkSQL";
    public static final String CONF_SPARK_DEFAULT_DB = "defaultDatabase";
    public static final String CONF_SPARK_DEFAULT_DB_DEFAULT = "default";
    public static final String CONF_SPARK_LISTENER_JAR = "ListenerJar";

    public static final String CONF_SPARK_DATASTORE_URL = "dataStoreUrl";

    // variables
    public static final String CONF_VARIABLES = "variables";

    //lineage
    public static final String CONF_LINEAGE_OUTPUT_PATH = "lineageOutputPath";
    public static final String CONF_LINEAGE_JAR_PATH = "lineageJarPath";
    public static final String CONF_S3_ACCESS_KEY = "s3AccessKey";
    public static final String CONF_S3_SECRET_KEY = "s3SecretKey";
    public static final String VAR_S3_ACCESS_KEY = "AWS_ACCESS_KEY_ID";
    public static final String VAR_S3_SECRET_KEY = "AWS_SECRET_ACCESS_KEY";

    public static final String CONF_LINEAGE_OUTPUT_PATH_VALUE_DEFAULT = "${ dataplatform.lineage.output.path }";
    public static final String CONF_S3_ACCESS_KEY_VALUE_DEFAULT = "${ dataplatform.s3.access.key }";
    public static final String CONF_S3_SECRET_KEY_VALUE_DEFAULT = "${ dataplatform.s3.secret.key }";
    public static final String CONF_LINEAGE_JAR_PATH_VALUE_DEFAULT = "${ dataplatform.lineage.analysis.jar }";

    //spark-submit
    public static final String SPARK_DEPLOY_MODE = "deploy-mode";
    public static final String SPARK_MASTER = "master";
    public static final String SPARK_PROXY_USER = "proxy-user";
    public static final String SPARK_PROXY_USER_DEFAULT_VALUE = "${ dataplatform.spark.proxy-user }";
    public static final String SPARK_YARN_HOST_DEFAULT_VALUE = "${ dataplatform.yarn.host }";
    public static final String SPARK_SQL_JAR_DEFAULT_VALUE = "${ dataplatform.spark.sql.jar }";
    public static final String SPARK_ENTRY_CLASS = "class";

    //data lake
    public static final String SPARK_DATA_LAKE_PACKAGES = "spark.datalake.packges";
    public static final String SPARK_DATA_LAKE_DEFAULT_PACKAGES = "${ dataplatform.datalake.packges }";
    public static final String SPARK_SERIALIZER = "spark.serializer";
    public static final String SPARK_DEFAULT_SERIALIZER = "${ dataplatform.spark.serializer }";
    public static final String SPARK_SQL_EXTENSIONS = "spark.sql.extensions";
    public static final String SPARK_DEFAULT_SQL_EXTENSIONS = "${ dataplatform.spark.sql.extensions }";

    public static final String SPARK_YARN_HOST = "yarnHost";
    public static final String SPARK_BASE_COMMAND = "sparkBaseCmd";
    public static final String SPARK_SUBMIT_PARMAS = "sparkSubmitParmas";
    public static final String SPARK_CONF = "sparkConf";
    public static final String KUN_SPARK_CONF = "kunSparkConf";
    public static final String DEFAULT_KUN_SPARK_CONF = "${ dataplatform.spark.default.conf }";
    public static final String SPARK_APPLICATION = "application";
    public static final String SPARK_APPLICATION_ARGS = "args";


    private SparkConfiguration() {
    }

    public static String getString(OperatorContext context, String key) {
        return context.getConfig().getString(key);
    }

    public static Boolean getBoolean(OperatorContext context, String key) {
        return context.getConfig().getBoolean(key);
    }

    public static Map<String, String> getVariables(OperatorContext context) {
        String vars = getString(context, CONF_VARIABLES);
        return JSONUtils.jsonToObject(vars, Map.class);
    }
}
