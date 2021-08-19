package com.miotech.kun.workflow.operator;

import com.google.common.base.Strings;
import com.miotech.kun.commons.utils.IdGenerator;
import com.miotech.kun.workflow.core.execution.*;
import com.miotech.kun.workflow.operator.spark.clients.SparkClient;
import com.miotech.kun.workflow.operator.spark.clients.YarnLoggerParser;
import com.miotech.kun.workflow.utils.JSONUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.StartedProcess;
import org.zeroturnaround.exec.stream.slf4j.Slf4jStream;

import java.io.*;
import java.util.*;
import java.util.concurrent.ExecutionException;

import static com.miotech.kun.workflow.operator.SparkConfiguration.*;

public class SparkSQLOperator extends LivyBaseSparkOperator {

    private String appId;
    private String yarnHost;
    private Process process;
    private SparkClient sparkClient;
    private OutputStream stderrStream = new ByteArrayOutputStream(1024 * 1024 * 10);
    private static final Logger logger = LoggerFactory.getLogger(SparkSQLOperator.class);
    private final YarnLoggerParser loggerParser = new YarnLoggerParser();


    @Override
    public void init() {
        OperatorContext context = getContext();
        logger.info("Recieved task config: {}", JSONUtils.toJsonString(context.getConfig()));

        yarnHost = SparkConfiguration.getString(context, SparkConfiguration.SPARK_YARN_HOST);
        if (Strings.isNullOrEmpty(yarnHost)) {
            String livyHost = SparkConfiguration.getString(context, CONF_LIVY_HOST);
            yarnHost = String.join(":", Arrays.copyOf(livyHost.split(":"), livyHost.split(":").length - 1));
        }
        sparkClient = new SparkClient(yarnHost);
    }



    @Override
    public boolean run() {

        OperatorContext context = getContext();
        Config config = context.getConfig();
        Long taskRunId = context.getTaskRunId();

        Map<String, String> sparkSubmitParams = new HashMap<>();

        String sparkConfStr = config.getString(CONF_LIVY_BATCH_CONF);
        Map<String, String> sparkConf = new HashMap<>();
        if (!Strings.isNullOrEmpty(sparkConfStr)) {
            sparkConf = JSONUtils.jsonStringToStringMap(sparkConfStr);
        }

        // add run time configs
        addRunTimeSparkConfs(sparkConf, context);
        addRunTimeParams(sparkSubmitParams, context, sparkConf);


        //build shell cmd
        List<String> cmd = buildCmd(sparkSubmitParams, sparkConf, config.getString(SPARK_APPLICATION), config.getString(CONF_SPARK_SQL));
        logger.info("execute cmd: " + String.join(" ", cmd));

        try {
            ProcessExecutor processExecutor = new ProcessExecutor();

            StartedProcess startedProcess = processExecutor
                    .environment(VAR_S3_ACCESS_KEY, config.getString(CONF_S3_ACCESS_KEY))
                    .environment(VAR_S3_SECRET_KEY, config.getString(CONF_S3_SECRET_KEY))
                    .command(cmd)
                    .redirectOutput(Slf4jStream.of(logger).asInfo())
                    .redirectError(stderrStream)
                    .redirectErrorAlsoTo(Slf4jStream.of(logger).asInfo())
                    .start();
            process = startedProcess.getProcess();

            boolean finalStatus = true;

            // wait for termination
            int exitCode = startedProcess.getFuture().get().getExitValue();
            logger.info("process exit code: {}", exitCode);
            finalStatus = (exitCode == 0);
            appId = SparkOperatorUtils.parseYarnAppId(stderrStream, logger);

            if (!Strings.isNullOrEmpty(appId)) {
                finalStatus = SparkOperatorUtils.trackYarnAppStatus(appId, sparkClient, logger, loggerParser);

            }


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
        } catch (IOException | ExecutionException e) {
            logger.error("{}", e);
            return false;
        } catch (InterruptedException e) {
            logger.error("{}", e);
            Thread.currentThread().interrupt();
            return false;
        }
    }

    @Override
    public void abort() {
        appId = SparkOperatorUtils.parseYarnAppId(stderrStream, logger);
        SparkOperatorUtils.abortSparkJob(appId, logger, sparkClient, process);
    }

    @Override
    public ConfigDef config() {
        return new ConfigDef()
                .define(CONF_LIVY_HOST, ConfigDef.Type.STRING, true, "Livy host to submit application, in the format `ip:port`", CONF_LIVY_HOST)
                .define(CONF_LIVY_YARN_QUEUE, ConfigDef.Type.STRING, CONF_LIVY_YARN_QUEUE_DEFAULT, true, "yarn queue name, default is `default`", CONF_LIVY_YARN_QUEUE)
                .define(CONF_LIVY_PROXY_USER, ConfigDef.Type.STRING, CONF_LIVY_PROXY_DEFAULT, true, "proxy use for livy", CONF_LIVY_PROXY_USER)
                .define(CONF_LIVY_BATCH_JARS, ConfigDef.Type.STRING, "", true, "Java application jar files", CONF_LIVY_BATCH_JARS)
                .define(CONF_LIVY_BATCH_FILES, ConfigDef.Type.STRING, "", true, "files to use, seperated with `,`, the first file would be used as main entry", CONF_LIVY_BATCH_FILES)
                .define(CONF_LIVY_SHARED_SESSION, ConfigDef.Type.BOOLEAN, false,true, "whether to use shared session in spark", CONF_LIVY_SHARED_SESSION)
                .define(CONF_LIVY_SHARED_SESSION_NAME, ConfigDef.Type.STRING, "",true, " shared session name if shared session enabled", CONF_LIVY_SHARED_SESSION_NAME)
                .define(CONF_SPARK_SQL, ConfigDef.Type.STRING, true, "SQL script", CONF_SPARK_SQL)
                .define(CONF_SPARK_DEFAULT_DB, ConfigDef.Type.STRING, CONF_SPARK_DEFAULT_DB_DEFAULT,true, "Default database name for a sql execution", CONF_SPARK_DEFAULT_DB)
                .define(CONF_LIVY_BATCH_CONF, ConfigDef.Type.STRING, "{}", true, "Extra spark configuration , in the format `{\"key\": \"value\"}`", CONF_LIVY_BATCH_CONF)
                .define(CONF_VARIABLES, ConfigDef.Type.STRING, "{}", true, "SQL variables, use like `select ${a}`, supply with {\"a\": \"b\"}", CONF_VARIABLES)
                .define(CONF_LINEAGE_OUTPUT_PATH, ConfigDef.Type.STRING, CONF_LINEAGE_OUTPUT_PATH_VALUE_DEFAULT, true, "file system address to store lineage analysis report, in the format `s3a://BUCKET/path` or `hdfs://host:port/path`", CONF_LINEAGE_OUTPUT_PATH)
                .define(CONF_LINEAGE_JAR_PATH, ConfigDef.Type.STRING, CONF_LINEAGE_JAR_PATH_VALUE_DEFAULT, true, "the jar used for lineage analysis, in the format `s3a://BUCKET/xxx/xxx.jar` or `hdfs://host:port/xxx/xxx.jar`", CONF_LINEAGE_JAR_PATH)
                .define(CONF_S3_ACCESS_KEY, ConfigDef.Type.STRING, CONF_S3_ACCESS_KEY_VALUE_DEFAULT, true, "if using s3 to store lineage analysis report, need s3 credentials", CONF_S3_ACCESS_KEY)
                .define(CONF_S3_SECRET_KEY, ConfigDef.Type.STRING, CONF_S3_SECRET_KEY_VALUE_DEFAULT, true, "if using s3 to store lineage analysis report, need s3 credentials", CONF_S3_SECRET_KEY)
                .define(SPARK_YARN_HOST, ConfigDef.Type.STRING, SPARK_YARN_HOST_DEFAULT_VALUE, true, "Yarn host to submit application, in the format `ip:port`", SPARK_YARN_HOST)
                .define(SPARK_APPLICATION, ConfigDef.Type.STRING, SPARK_SQL_JAR_DEFAULT_VALUE, true, "application class name for java application", SPARK_APPLICATION);
    }

    @Override
    public Resolver getResolver() {
        return new NopResolver();
    }

    public void addRunTimeParams(Map<String, String> sparkSubmitParams, OperatorContext context, Map<String, String> sparkConf) {
        String proxyuser = context.getConfig().getString(CONF_LIVY_PROXY_USER);
        if (!Strings.isNullOrEmpty(proxyuser)) {
            sparkSubmitParams.put(SPARK_PROXY_USER, proxyuser);
        }

        if (!sparkConf.containsKey("spark.submit.deployMode")) {
            sparkSubmitParams.put(SPARK_DEPLOY_MODE, "cluster");
        }
        if (!sparkConf.containsKey("spark.master")) {
            sparkSubmitParams.put(SPARK_MASTER, "yarn");
        }

        String sessionName = SparkConfiguration.getString(context, CONF_LIVY_SHARED_SESSION_NAME);
        if (!Strings.isNullOrEmpty(sessionName)) {
            sessionName = sessionName + " - " + IdGenerator.getInstance().nextId();
        }else{
            sessionName = "Spark Job: " + IdGenerator.getInstance().nextId();
        }
        sparkSubmitParams.put("name", sessionName);

        //TODO: make entry class configurable
        sparkSubmitParams.put(SPARK_ENTRY_CLASS, "com.miotech.kun.sql.Application");

    }

    public void addRunTimeSparkConfs(Map<String, String> sparkConf, OperatorContext context) {

        Long taskRunId = context.getTaskRunId();
        sparkConf.put("spark.hadoop.taskRunId", taskRunId.toString());

        // lineage conf
        String configLineageOutputPath = SparkConfiguration.getString(context, CONF_LINEAGE_OUTPUT_PATH);
        String configLineageJarPath = SparkConfiguration.getString(context, CONF_LINEAGE_JAR_PATH);
        String configS3AccessKey = SparkConfiguration.getString(context, CONF_S3_ACCESS_KEY);
        String configS3SecretKey = SparkConfiguration.getString(context, CONF_S3_SECRET_KEY);

        List<String> allJars = new ArrayList<>();
        if (sparkConf.containsKey("spark.jars")) {
            allJars.addAll(Arrays.asList(sparkConf.get("spark.jars").split(",")));
        }
        if (!Strings.isNullOrEmpty(configLineageJarPath)) {
            allJars.add(configLineageJarPath);
            sparkConf.put("spark.sql.queryExecutionListeners", "za.co.absa.spline.harvester.listener.SplineQueryExecutionListener");
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

        // parse jars
        String jars = SparkConfiguration.getString(context, CONF_LIVY_BATCH_JARS);
        if (!Strings.isNullOrEmpty(jars)) {
            allJars.addAll(Arrays.asList(jars.split(",")));
        }
        if(!allJars.isEmpty()){
            sparkConf.put("spark.jars", String.join(",", allJars));
        }

        if (!sparkConf.containsKey("spark.driver.memory")) {
            sparkConf.put("spark.driver.memory", "2g");
        }

    }


    public List<String> buildCmd(Map<String, String> sparkSubmitParams, Map<String, String> sparkConf, String app, String appArgs) {
        List<String> cmd = new ArrayList<>();
        cmd.add("spark-submit");
        cmd.addAll(SparkOperatorUtils.parseSparkSubmitParmas(sparkSubmitParams));

        File sqlFile = storeSqlToFile(appArgs);
        addSqlFile(sparkConf, sqlFile.getPath());
        cmd.addAll(SparkOperatorUtils.parseSparkConf(sparkConf));
        cmd.add(app);
        cmd.add("-f");
        cmd.add(sqlFile.getName());
        return cmd;
    }

    private void addSqlFile(Map<String, String> sparkConf, String sqlFile) {
        String files = sparkConf.getOrDefault("spark.files", "");
        if (files.equals("")) {
            sparkConf.put("spark.files", sqlFile);
        } else {
            sparkConf.put("spark.files", files + "," + sqlFile);
        }
    }

    private File storeSqlToFile(String sql) {
        File sqlFile = null;
        try {
            sqlFile = File.createTempFile("spark-sql-", ".sql");
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(sqlFile))) {
                writer.write(sql);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sqlFile;
    }

}
