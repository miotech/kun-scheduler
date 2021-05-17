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
    public static final String CONF_LIVY_BATCH_ARGS = "args";
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
    public static final String CONF_S3_ACCESS_KEY = "s3AccessKey";
    public static final String CONF_S3_SECRET_KEY = "s3SecretKey";

    public static final String CONF_LINEAGE_OUTPUT_PATH_VALUE_DEFAULT = "${ dataplatform.lineage.output.path }";
    public static final String CONF_S3_ACCESS_KEY_VALUE_DEFAULT = "${ dataplatform.s3.access.key }";
    public static final String CONF_S3_SECRET_KEY_VALUE_DEFAULT = "${ dataplatform.s3.secret.key }";


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
