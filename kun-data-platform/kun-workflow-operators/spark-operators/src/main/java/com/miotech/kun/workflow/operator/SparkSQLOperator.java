package com.miotech.kun.workflow.operator;

import com.google.common.base.Strings;
import com.miotech.kun.workflow.core.execution.*;
import com.miotech.kun.workflow.utils.JSONUtils;
import org.joor.Reflect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static com.miotech.kun.workflow.operator.SparkConfiguration.*;

public class SparkSQLOperator extends KunOperator {

    private static final Logger logger = LoggerFactory.getLogger(SparkSQLOperator.class);
    private SparkSqlOperatorV1 sparkSqlOperatorV1 = new SparkSqlOperatorV1();
    private SparkSqlOperatorV2 sparkSqlOperatorV2 = new SparkSqlOperatorV2();
    private final int shadowTestPct = 5;
    private boolean useLegacySparkSqlOperator;

    @Override
    public void init() {
        OperatorContext context = getContext();
        logger.info("Recieved task config: {}", JSONUtils.toJsonString(context.getConfig()));

        if (context.getTaskRunId() % 10 >= shadowTestPct) {
            useLegacySparkSqlOperator = true;
            sparkSqlOperatorV1.setContext(context);
            sparkSqlOperatorV1.init();
        } else {
            useLegacySparkSqlOperator = false;
            Config newConfig = transformOperatorConfig(context);
            Reflect.on(context).set("config", newConfig);
            sparkSqlOperatorV2.setContext(context);
            sparkSqlOperatorV2.init();
        }
    }

    @Override
    public boolean run() {
        if (useLegacySparkSqlOperator) {
            return sparkSqlOperatorV1.run();
        } else {
            return sparkSqlOperatorV2.run();
        }
    }

    @Override
    public void abort() {
        if (useLegacySparkSqlOperator) {
            sparkSqlOperatorV1.abort();
        } else {
            sparkSqlOperatorV2.abort();
        }
    }

    @Override
    public Optional<TaskAttemptReport> getReport() {
        if (useLegacySparkSqlOperator) {
            return sparkSqlOperatorV1.getReport();
        } else {
            return sparkSqlOperatorV2.getReport();
        }
    }

    @Override
    public ConfigDef config() {
        return new ConfigDef()
                .define(CONF_LIVY_HOST, ConfigDef.Type.STRING, true, "Livy host to submit application, in the format `ip:port`", CONF_LIVY_HOST)
                .define(CONF_LIVY_YARN_QUEUE, ConfigDef.Type.STRING, CONF_LIVY_YARN_QUEUE_DEFAULT, true, "yarn queue name, default is `default`", CONF_LIVY_YARN_QUEUE)
                .define(CONF_LIVY_PROXY_USER, ConfigDef.Type.STRING, SPARK_PROXY_USER_DEFAULT_VALUE, true, "proxy use for livy", CONF_LIVY_PROXY_USER)
                .define(CONF_LIVY_BATCH_JARS, ConfigDef.Type.STRING, "", true, "Java application jar files", CONF_LIVY_BATCH_JARS)
                .define(CONF_LIVY_BATCH_FILES, ConfigDef.Type.STRING, "", true, "files to use, seperated with `,`, the first file would be used as main entry", CONF_LIVY_BATCH_FILES)
                .define(CONF_LIVY_SHARED_SESSION, ConfigDef.Type.BOOLEAN, false, true, "whether to use shared session in spark", CONF_LIVY_SHARED_SESSION)
                .define(CONF_LIVY_SHARED_SESSION_NAME, ConfigDef.Type.STRING, "", true, " shared session name if shared session enabled", CONF_LIVY_SHARED_SESSION_NAME)
                .define(CONF_SPARK_SQL, ConfigDef.Type.STRING, true, "SQL script", CONF_SPARK_SQL)
                .define(CONF_SPARK_DEFAULT_DB, ConfigDef.Type.STRING, CONF_SPARK_DEFAULT_DB_DEFAULT, true, "Default database name for a sql execution", CONF_SPARK_DEFAULT_DB)
                .define(CONF_LIVY_BATCH_CONF, ConfigDef.Type.STRING, "{}", true, "Extra spark configuration , in the format `{\"key\": \"value\"}`", CONF_LIVY_BATCH_CONF)
                .define(CONF_VARIABLES, ConfigDef.Type.STRING, "{}", true, "SQL variables, use like `select ${a}`, supply with {\"a\": \"b\"}", CONF_VARIABLES)
                .define(CONF_LINEAGE_OUTPUT_PATH, ConfigDef.Type.STRING, CONF_LINEAGE_OUTPUT_PATH_VALUE_DEFAULT, true, "file system address to store lineage analysis report, in the format `s3a://BUCKET/path` or `hdfs://host:port/path`", CONF_LINEAGE_OUTPUT_PATH)
                .define(CONF_LINEAGE_JAR_PATH, ConfigDef.Type.STRING, CONF_LINEAGE_JAR_PATH_VALUE_DEFAULT, true, "the jar used for lineage analysis, in the format `s3a://BUCKET/xxx/xxx.jar` or `hdfs://host:port/xxx/xxx.jar`", CONF_LINEAGE_JAR_PATH)
                .define(CONF_S3_ACCESS_KEY, ConfigDef.Type.STRING, CONF_S3_ACCESS_KEY_VALUE_DEFAULT, true, "if using s3 to store lineage analysis report, need s3 credentials", CONF_S3_ACCESS_KEY)
                .define(CONF_S3_SECRET_KEY, ConfigDef.Type.STRING, CONF_S3_SECRET_KEY_VALUE_DEFAULT, true, "if using s3 to store lineage analysis report, need s3 credentials", CONF_S3_SECRET_KEY)
                .define(SPARK_YARN_HOST, ConfigDef.Type.STRING, SPARK_YARN_HOST_DEFAULT_VALUE, true, "Yarn host to submit application, in the format `ip:port`", SPARK_YARN_HOST)
                .define(SPARK_APPLICATION, ConfigDef.Type.STRING, SPARK_SQL_JAR_DEFAULT_VALUE, true, "application class name for java application", SPARK_APPLICATION)
                .define(SPARK_DATA_LAKE_PACKAGES, ConfigDef.Type.STRING, SPARK_DATA_LAKE_DEFAULT_PACKAGES, true, "data lake dependencies", SPARK_DATA_LAKE_PACKAGES)
                .define(SPARK_SQL_EXTENSIONS, ConfigDef.Type.STRING, SPARK_DEFAULT_SQL_EXTENSIONS, true, "spark sql extensions", SPARK_SQL_EXTENSIONS)
                .define(SPARK_SERIALIZER, ConfigDef.Type.STRING, SPARK_DEFAULT_SERIALIZER, true, "spark serializer", SPARK_SERIALIZER)
                .define(KUN_SPARK_CONF, ConfigDef.Type.STRING, DEFAULT_KUN_SPARK_CONF, true, "default key-value spark conf", KUN_SPARK_CONF);
    }

    @Override
    public Resolver getResolver() {
        return new NopResolver();
    }

    Config transformOperatorConfig(OperatorContext context) {
        Config config = context.getConfig();

        Map<String, String> sparkConf = generateRunTimeSparkConfs(config);
        Map<String, String> sparkSubmitParmas = generateRunTimeParams(config);

        Config.Builder builder = Config.newBuilder()
                .addConfig(SPARK_SUBMIT_PARMAS, JSONUtils.toJsonString(sparkSubmitParmas))
                .addConfig(SPARK_CONF, JSONUtils.toJsonString(sparkConf))
                .addConfig(SPARK_PROXY_USER, config.getString(CONF_LIVY_PROXY_USER))
                .addConfig(SPARK_APPLICATION, config.getString(SPARK_APPLICATION))
                .addConfig(SPARK_APPLICATION_ARGS, config.getString(CONF_SPARK_SQL))
                .addConfig(SPARK_YARN_HOST, config.getString(SPARK_YARN_HOST))
                .addConfig(CONF_LINEAGE_OUTPUT_PATH, config.getString(CONF_LINEAGE_OUTPUT_PATH))
                .addConfig(CONF_LINEAGE_JAR_PATH, config.getString(CONF_LINEAGE_JAR_PATH))
                .addConfig(CONF_S3_ACCESS_KEY, config.getString(CONF_S3_ACCESS_KEY))
                .addConfig(CONF_S3_SECRET_KEY, config.getString(CONF_S3_SECRET_KEY))
                .addConfig(SPARK_DATA_LAKE_PACKAGES, config.getString(SPARK_DATA_LAKE_PACKAGES))
                .addConfig(SPARK_SQL_EXTENSIONS, config.getString(SPARK_SQL_EXTENSIONS))
                .addConfig(SPARK_SERIALIZER, config.getString(SPARK_SERIALIZER))
                .addConfig(KUN_SPARK_CONF,config.getString(KUN_SPARK_CONF));
        return builder.build();
    }

    public Map<String, String> generateRunTimeParams(Config config) {
        Map<String, String> sparkSubmitParams = new HashMap<>();

        OperatorContext context = getContext();
        String sessionName = config.getString(CONF_LIVY_SHARED_SESSION_NAME);
        if (!Strings.isNullOrEmpty(sessionName)) {
            sessionName = sessionName + " - " + context.getTaskRunId();
        } else {
            sessionName = "Spark Job: " + context.getTaskRunId();
        }
        sparkSubmitParams.put("name", sessionName);

        //TODO: make entry class configurable
        sparkSubmitParams.put(SPARK_ENTRY_CLASS, "com.miotech.kun.sql.Application");
        return sparkSubmitParams;

    }

    public Map<String, String> generateRunTimeSparkConfs(Config config) {
        String sparkConfStr = config.getString(CONF_LIVY_BATCH_CONF);
        Map<String, String> sparkConf = null;
        if (!Strings.isNullOrEmpty(sparkConfStr)) {
            sparkConf = JSONUtils.jsonStringToStringMap(sparkConfStr);
        }
        if (sparkConf == null) {
            sparkConf = new HashMap<>();
        }

        // parse jars
        List<String> allJars = new ArrayList<>();
        if (sparkConf.containsKey("spark.jars")) {
            allJars.addAll(Arrays.asList(sparkConf.get("spark.jars").split(",")));
        }
        String jars = config.getString(CONF_LIVY_BATCH_JARS);
        if (!Strings.isNullOrEmpty(jars)) {
            allJars.addAll(Arrays.asList(jars.split(",")));
        }
        if (!allJars.isEmpty()) {
            sparkConf.put("spark.jars", String.join(",", allJars));
        }

        return sparkConf;
    }
}
