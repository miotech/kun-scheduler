package com.miotech.kun.workflow.operator;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.miotech.kun.workflow.core.execution.*;
import com.miotech.kun.workflow.utils.JSONUtils;
import org.apache.commons.collections.CollectionUtils;
import org.joor.Reflect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

import static com.miotech.kun.workflow.operator.SparkConfiguration.*;

public class SparkOperator extends KunOperator {

    private String entryFile;
    private static final Logger logger = LoggerFactory.getLogger(SparkOperator.class);

    private boolean useLegacySparkOperator;
    private final int shadowTestPct = 5;
    private SparkOperatorV1 sparkOperatorV1 = new SparkOperatorV1();
    private SparkOperatorV2 sparkOperatorV2 = new SparkOperatorV2();

    @Override
    public void init() {
        OperatorContext context = getContext();
        logger.info("Recieved task config: {}", JSONUtils.toJsonString(context.getConfig()));

        if (context.getTaskRunId() % 10 >= shadowTestPct) {
            useLegacySparkOperator = true;
            sparkOperatorV1.setContext(context);
            sparkOperatorV1.init();
        } else {
            useLegacySparkOperator = false;
            Config newConfig = transformOperatorConfig(context);
            Reflect.on(context).set("config", newConfig);
            sparkOperatorV2.setContext(context);
            sparkOperatorV2.init();
        }
    }

    @Override
    public boolean run() {
        if (useLegacySparkOperator) {
            return sparkOperatorV1.run();
        } else {
            return sparkOperatorV2.run();
        }

    }

    @Override
    public void abort() {
        if (useLegacySparkOperator) {
            sparkOperatorV1.abort();
        } else {
            sparkOperatorV2.abort();
        }
    }

    @Override
    public Optional<TaskAttemptReport> getReport() {
        if (useLegacySparkOperator) {
            return sparkOperatorV1.getReport();
        } else {
            return sparkOperatorV2.getReport();
        }
    }

    Config transformOperatorConfig(OperatorContext context) {
        Config config = context.getConfig();

        Map<String, String> sparkConf = generateRunTimeSparkConfs(config);
        Map<String, String> sparkSubmitParmas = generateRunTimeParams(config);

        Config.Builder builder = Config.newBuilder()
                .addConfig(SPARK_SUBMIT_PARMAS, JSONUtils.toJsonString(sparkSubmitParmas))
                .addConfig(SPARK_CONF, JSONUtils.toJsonString(sparkConf))
                .addConfig(SPARK_PROXY_USER, config.getString(CONF_LIVY_PROXY_USER))
                .addConfig(SPARK_APPLICATION, entryFile)
                .addConfig(SPARK_APPLICATION_ARGS, config.getString(SPARK_APPLICATION_ARGS))
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
        String sessionName = config.getString(CONF_LIVY_BATCH_NAME);
        if (!Strings.isNullOrEmpty(sessionName)) {
            sessionName = sessionName + "-" + context.getTaskRunId();
            sparkSubmitParams.put("name", sessionName);
        }

        String application = config.getString(CONF_LIVY_BATCH_APPLICATION);
        if (!Strings.isNullOrEmpty(application)) {
            sparkSubmitParams.put(SPARK_ENTRY_CLASS, application);
        }
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

        // parse entry jar/py file
        String files = config.getString(CONF_LIVY_BATCH_FILES);
        List<String> jobFiles = new ArrayList<>();
        if (!Strings.isNullOrEmpty(files)) {
            jobFiles = Arrays.stream(files.split(","))
                    .map(String::trim)
                    .filter(x -> !x.isEmpty())
                    .collect(Collectors.toList());
        }
        if (!jobFiles.isEmpty()) {
            String mainEntry = jobFiles.get(0);
            boolean isJava;
            isJava = mainEntry.endsWith(".jar");
            entryFile = mainEntry;
            logger.info("Find main entry file : {}", mainEntry);
            // set extra files
            List<String> extraFiles = jobFiles.size() > 1 ? jobFiles.subList(1, jobFiles.size()) : ImmutableList.of();
            if (!CollectionUtils.isEmpty(extraFiles)) {
                if (isJava) {
                    List<String> allFiles = sparkConf.containsKey("spark.files") ? Arrays.asList(sparkConf.get("spark.files").split(",")) : new ArrayList<>();
                    allFiles.addAll(extraFiles);
                    sparkConf.put("spark.files", String.join(",", allFiles));
                } else {
                    List<String> allPyFiles = sparkConf.containsKey("spark.submit.pyFiles") ? Arrays.asList(sparkConf.get("spark.submit.pyFiles").split(",")) : new ArrayList<>();
                    allPyFiles.addAll(extraFiles);
                    sparkConf.put("spark.submit.pyFiles", String.join(",", allPyFiles));
                }
            }
        }
        return sparkConf;
    }

    @Override
    public ConfigDef config() {
        return new ConfigDef()
                .define(CONF_LIVY_HOST, ConfigDef.Type.STRING, true, "Livy host to submit application, in the format `ip:port`", CONF_LIVY_HOST)
                .define(CONF_LIVY_YARN_QUEUE, ConfigDef.Type.STRING, CONF_LIVY_YARN_QUEUE_DEFAULT, true, "yarn queue name, default is `default`", CONF_LIVY_YARN_QUEUE)
                .define(CONF_LIVY_PROXY_USER, ConfigDef.Type.STRING, SPARK_PROXY_USER_DEFAULT_VALUE, true, "proxy use for livy", CONF_LIVY_PROXY_USER)
                .define(CONF_LIVY_BATCH_JARS, ConfigDef.Type.STRING, "", true, "Java application jar files", CONF_LIVY_BATCH_JARS)
                .define(CONF_LIVY_BATCH_FILES, ConfigDef.Type.STRING, "", true, "files to use, seperated with `,`, the first file would be used as main entry", CONF_LIVY_BATCH_FILES)
                .define(CONF_LIVY_BATCH_APPLICATION, ConfigDef.Type.STRING, "", true, "application class name for java application", CONF_LIVY_BATCH_APPLICATION)
                .define(CONF_LIVY_BATCH_ARGS, ConfigDef.Type.STRING, "", true, "application arguments", CONF_LIVY_BATCH_ARGS)
                .define(CONF_LIVY_BATCH_NAME, ConfigDef.Type.STRING, "", true, "application session name", CONF_LIVY_BATCH_NAME)
                .define(CONF_LIVY_BATCH_CONF, ConfigDef.Type.STRING, "{}", true, "Extra spark configuration , in the format `{\"key\": \"value\"}`", CONF_LIVY_BATCH_CONF)
                .define(CONF_VARIABLES, ConfigDef.Type.STRING, "{}", true, "Spark arguments and configuration variables, use like `--param1 ${a}`, supply with {\"a\": \"b\"}", CONF_VARIABLES)
                .define(CONF_LINEAGE_OUTPUT_PATH, ConfigDef.Type.STRING, CONF_LINEAGE_OUTPUT_PATH_VALUE_DEFAULT, true, "file system address to store lineage analysis report, in the format `s3a://BUCKET/path` or `hdfs://host:port/path`", CONF_LINEAGE_OUTPUT_PATH)
                .define(CONF_LINEAGE_JAR_PATH, ConfigDef.Type.STRING, CONF_LINEAGE_JAR_PATH_VALUE_DEFAULT, true, "the jar used for lineage analysis, in the format `s3a://BUCKET/xxx/xxx.jar` or `hdfs://host:port/xxx/xxx.jar`", CONF_LINEAGE_JAR_PATH)
                .define(CONF_S3_ACCESS_KEY, ConfigDef.Type.STRING, CONF_S3_ACCESS_KEY_VALUE_DEFAULT, true, "if using s3 to store lineage analysis report, need s3 credentials", CONF_S3_ACCESS_KEY)
                .define(CONF_S3_SECRET_KEY, ConfigDef.Type.STRING, CONF_S3_SECRET_KEY_VALUE_DEFAULT, true, "if using s3 to store lineage analysis report, need s3 credentials", CONF_S3_SECRET_KEY)
                .define(SPARK_YARN_HOST, ConfigDef.Type.STRING, SPARK_YARN_HOST_DEFAULT_VALUE, true, "Yarn host to submit application, in the format `ip:port`", SPARK_YARN_HOST)
                .define(SPARK_DATA_LAKE_PACKAGES, ConfigDef.Type.STRING, SPARK_DATA_LAKE_DEFAULT_PACKAGES, true, "data lake dependencies", SPARK_DATA_LAKE_PACKAGES)
                .define(SPARK_SQL_EXTENSIONS, ConfigDef.Type.STRING, SPARK_DEFAULT_SQL_EXTENSIONS, true, "spark sql extensions", SPARK_SQL_EXTENSIONS)
                .define(SPARK_SERIALIZER, ConfigDef.Type.STRING, SPARK_DEFAULT_SERIALIZER, true, "spark serializer", SPARK_SERIALIZER)
                .define(KUN_SPARK_CONF, ConfigDef.Type.STRING, DEFAULT_KUN_SPARK_CONF, true, "default key-value spark conf", KUN_SPARK_CONF);
    }

    @Override
    public Resolver getResolver() {
        // TODO: implement this
        return new NopResolver();
    }

}
