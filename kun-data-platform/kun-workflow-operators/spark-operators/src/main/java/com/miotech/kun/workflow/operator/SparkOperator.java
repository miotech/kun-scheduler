package com.miotech.kun.workflow.operator;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.miotech.kun.commons.utils.IdGenerator;
import com.miotech.kun.workflow.core.execution.*;
import com.miotech.kun.workflow.operator.spark.clients.SparkClient;
import com.miotech.kun.workflow.operator.spark.clients.YarnLoggerParser;
import com.miotech.kun.workflow.utils.JSONUtils;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.StartedProcess;
import org.zeroturnaround.exec.stream.slf4j.Slf4jStream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static com.miotech.kun.workflow.operator.SparkConfiguration.*;

public class SparkOperator extends KunOperator {

    private String appId;
    private String yarnHost;
    private String entryFile;
    private Process process;
    private SparkClient sparkClient;
    private OutputStream stderrStream = new ByteArrayOutputStream(1024 * 1024 * 10);
    private static final Logger logger = LoggerFactory.getLogger(SparkOperator.class);
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
        List<String> cmd = buildCmd(sparkSubmitParams, sparkConf, entryFile, config.getString(CONF_LIVY_BATCH_ARGS));
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

        String sessionName = SparkConfiguration.getString(context, CONF_LIVY_BATCH_NAME);
        if (!Strings.isNullOrEmpty(sessionName)) {
            sessionName = sessionName + "-" + IdGenerator.getInstance().nextId();
            sparkSubmitParams.put("name", sessionName);
        }

        String application = SparkConfiguration.getString(context, CONF_LIVY_BATCH_APPLICATION);
        if (!Strings.isNullOrEmpty(application)) {
            sparkSubmitParams.put(SPARK_ENTRY_CLASS, application);
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
        sparkConf.put("spark.jars", String.join(",", allJars));

        // parse entry jar/py file
        String files = SparkConfiguration.getString(context, CONF_LIVY_BATCH_FILES);
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
                    List<String> allFiles = Arrays.asList(sparkConf.getOrDefault("spark.files", "").split(","));
                    allFiles.addAll(extraFiles);
                    sparkConf.put("spark.files", String.join(",", allFiles));
                } else {
                    List<String> allPyFiles = Arrays.asList(sparkConf.getOrDefault("spark.submit.pyFiles", "").split(","));
                    allPyFiles.addAll(extraFiles);
                    sparkConf.put("spark.submit.pyFiles", String.join(",", allPyFiles));
                }
            }
        }

        if (!sparkConf.containsKey("spark.driver.memory")) {
            sparkConf.put("spark.driver.memory", "2g");
        }
    }

    public List<String> buildCmd(Map<String, String> sparkSubmitParams, Map<String, String> sparkConf, String app, String appArgs) {
        List<String> cmd = new ArrayList<>();
        cmd.add("spark-submit");
        cmd.addAll(SparkOperatorUtils.parseSparkSubmitParmas(sparkSubmitParams));
        cmd.addAll(SparkOperatorUtils.parseSparkConf(sparkConf));
        cmd.add(app);
        cmd.addAll(Arrays.asList(appArgs.split(" ")));
        return cmd;
    }

    @Override
    public ConfigDef config() {
        return new ConfigDef()
                .define(CONF_LIVY_HOST, ConfigDef.Type.STRING, true, "Livy host to submit application, in the format `ip:port`", CONF_LIVY_HOST)
                .define(CONF_LIVY_YARN_QUEUE, ConfigDef.Type.STRING, CONF_LIVY_YARN_QUEUE_DEFAULT, true, "yarn queue name, default is `default`", CONF_LIVY_YARN_QUEUE)
                .define(CONF_LIVY_PROXY_USER, ConfigDef.Type.STRING, CONF_LIVY_PROXY_DEFAULT, true, "proxy use for livy", CONF_LIVY_PROXY_USER)
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
                .define(SPARK_YARN_HOST, ConfigDef.Type.STRING, SPARK_YARN_HOST_DEFAULT_VALUE, true, "Yarn host to submit application, in the format `ip:port`", SPARK_YARN_HOST);
    }

    @Override
    public Resolver getResolver() {
        // TODO: implement this
        return new NopResolver();
    }

}
