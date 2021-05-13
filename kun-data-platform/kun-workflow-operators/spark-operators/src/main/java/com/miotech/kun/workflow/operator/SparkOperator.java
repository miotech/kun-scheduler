package com.miotech.kun.workflow.operator;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.miotech.kun.commons.utils.IdGenerator;
import com.miotech.kun.metadata.core.model.DataStore;
import com.miotech.kun.workflow.core.execution.*;
import com.miotech.kun.workflow.operator.resolver.SparkOperatorResolver;
import com.miotech.kun.workflow.operator.spark.clients.YarnLoggerParser;
import com.miotech.kun.workflow.operator.spark.models.AppInfo;
import com.miotech.kun.workflow.operator.spark.models.SparkApp;
import com.miotech.kun.workflow.operator.spark.models.SparkJob;
import com.miotech.kun.workflow.operator.spark.models.StateInfo;
import com.miotech.kun.workflow.utils.JSONUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.miotech.kun.workflow.operator.SparkConfiguration.*;

public class SparkOperator extends LivyBaseSparkOperator {
    private static final Logger logger = LoggerFactory.getLogger(SparkOperator.class);
    private final YarnLoggerParser loggerParser = new YarnLoggerParser();
    private final String SPLINE_QUERY_LISTENER = "za.co.absa.spline.harvester.listener.SplineQueryExecutionListener";
    private final String SPARK_QUERY_LISTENER = "spark.sql.queryExecutionListeners";
    private final String SPLINE_QUERY_LISTENER_PATH = "s3://com.miotech.data.prd/spline/spark-2.4-spline-agent-bundle_2.11-0.6.0-SNAPSHOT.jar";
    private final String HDFS_ROOT = "s3a://com.miotech.data.prd";
    private final Integer LIVY_TIMEOUT_LIMIT = 3;
    private volatile boolean cancelled = false;


    private SparkApp app;

    @Override
    public boolean run() {
        OperatorContext context = getContext();
        logger.info("Start init spark job params");

        String jars = SparkConfiguration.getString(context, CONF_LIVY_BATCH_JARS);
        String files = SparkConfiguration.getString(context, CONF_LIVY_BATCH_FILES);
        String application = SparkConfiguration.getString(context, CONF_LIVY_BATCH_APPLICATION);
        String args = SparkConfiguration.getString(context, CONF_LIVY_BATCH_ARGS);
        String sparkConf = SparkConfiguration.getString(context, CONF_LIVY_BATCH_CONF);
        Long taskRunId = context.getTaskRunId();

        // should using task name
        String sessionName = SparkConfiguration.getString(context, CONF_LIVY_BATCH_NAME);
        if (Strings.isNullOrEmpty(sessionName)) {
            sessionName = "Spark Job: " + IdGenerator.getInstance().nextId();
        } else {
            sessionName = sessionName + " - " + IdGenerator.getInstance().nextId();
        }

        SparkJob job = new SparkJob();
        if (!Strings.isNullOrEmpty(sessionName)) {
            job.setName(sessionName);
        }
        jars = jars + "," + SPLINE_QUERY_LISTENER_PATH;
        if (!Strings.isNullOrEmpty(jars)) {
            job.setJars(Arrays.asList(jars.split(",")));
        }
        List<String> jobFiles = new ArrayList<>();
        if (!Strings.isNullOrEmpty(files)) {
            jobFiles = Arrays.stream(files.split(","))
                    .map(String::trim)
                    .filter(x -> !x.isEmpty())
                    .collect(Collectors.toList());
        }
        if (!Strings.isNullOrEmpty(sparkConf)) {
            job.setConf(JSONUtils.jsonStringToStringMap(replaceWithVariable(sparkConf)));
        }
        job.addConf(SPARK_QUERY_LISTENER, SPLINE_QUERY_LISTENER);
        job.addConf("spark.hadoop.taskRunId", taskRunId.toString());
        if (!job.getConf().containsKey("spark.driver.memory")) {
            job.addConf("spark.driver.memory", "2g");
        }
        if (!jobFiles.isEmpty()) {
            String mainEntry = jobFiles.get(0);
            boolean isJava;
            isJava = mainEntry.endsWith(".jar");
            job.setFile(mainEntry);
            logger.info("Find main entry file : {}", mainEntry);
            // set extra files
            List<String> extraFiles = jobFiles.size() > 1 ? jobFiles.subList(1, jobFiles.size()) : ImmutableList.of();
            if (!CollectionUtils.isEmpty(extraFiles)) {
                if (isJava) {
                    job.setFiles(extraFiles);
                } else {
                    job.setPyFiles(extraFiles);
                }
            }
        }
        if (!Strings.isNullOrEmpty(application)) {
            job.setClassName(application);
        }
        List<String> jobArgs = new ArrayList<>();
        if (!Strings.isNullOrEmpty(args)) {
            jobArgs = Arrays.stream(Arrays.stream(args.split("\\s+"))
                    .filter(x -> !x.isEmpty())
                    .map(String::trim).toArray(String[]::new))
                    .map(this::replaceWithVariable)
                    .collect(Collectors.toList());
        }

        if (!jobArgs.isEmpty()) {
            job.setArgs(jobArgs);
        }

        logger.info("Execution job : {}", JSONUtils.toJsonString(job));
        app = livyClient.runSparkJob(job);
        int jobId = app.getId();
        logger.info("Execute spark application using livy : batch id {}", jobId);

        StateInfo.State jobState = null;
        String applicationId = null;
        AppInfo appInfo = null;
        Integer timeout = 0;
        do {
            try {
                if (StringUtils.isEmpty(applicationId)
                        || StringUtils.isEmpty(app.getAppInfo().getDriverLogUrl())) {
                    app = livyClient.getSparkJob(jobId);
                    applicationId = app.getAppId();
                    appInfo = app.getAppInfo();
                    if (!StringUtils.isEmpty(applicationId)) {
                        logger.info("Application info: {}", JSONUtils.toJsonString(app));
                    }
                }
                jobState = livyClient.getSparkJobState(app.getId()).getState();
                timeout = 0;
            } catch (RuntimeException e) {
                timeout++;
                logger.warn("get job information from livy timeout, times = {}", timeout);
                if (timeout >= LIVY_TIMEOUT_LIMIT) {
                    logger.error("get job information from livy failed", e);
                    throw e;
                }
            }
            if (jobState == null) {
                throw new IllegalStateException("Cannot find state for job: " + app.getId());
            }
            waitForSeconds(3);
        } while (!cancelled && !jobState.isFinished());

        tailingYarnLog(appInfo);
        logger.info("spark job \"{}\", batch id: {}", jobState, jobId);
        if (jobState.equals(StateInfo.State.SUCCESS)) {
            //wait spline send execPlan
            waitForSeconds(15);
            //解析spark 任务上下游
            lineageAnalysis(context.getConfig(), taskRunId);
            return true;
        } else {
            return false;
        }
    }

    private void tailingYarnLog(AppInfo app) {
        try {
            String logUrl = app.getDriverLogUrl();
            logger.info("Fetch log from {}", logUrl);
            logger.info(loggerParser.getYarnLogs(logUrl));
        } catch (Exception e) {
            logger.error("Error in fetch application logs, {}", e);
        }
    }

    @Override
    public void abort() {
        logger.info("Delete spark batch job, id: {}", app.getId());
        cancelled = true;
        livyClient.deleteBatch(app.getId());
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
                .define(CONF_VARIABLES, ConfigDef.Type.STRING, "{}", true, "Spark arguments and configuration variables, use like `--param1 ${a}`, supply with {\"a\": \"b\"}", CONF_VARIABLES);
    }

    @Override
    public Resolver getResolver() {
        // TODO: implement this
        return new NopResolver();
    }

    public void lineageAnalysis(Config config, Long taskRunId) {
        try {
            String sparkConf = config.getString(SparkConfiguration.CONF_LIVY_BATCH_CONF);
            logger.debug("spark conf = {}", sparkConf);
            Configuration conf = new Configuration();
            String configS3AccessKey = "fs.s3a.access.key";
            String configS3SecretKey = "fs.s3a.secret.key";
            conf.set(configS3AccessKey, "***REMOVED***");
            conf.set(configS3SecretKey, "O10ChEQ5u5jRJ8IOuypKZar/0ASaGcTAPaFG6yTt");
            conf.set("fs.hdfs.impl", org.apache.hadoop.hdfs.DistributedFileSystem.class.getName());
            conf.set("fs.file.impl", org.apache.hadoop.fs.LocalFileSystem.class.getName());
            HdfsFileSystem hdfsFileSystem = new HdfsFileSystem(HDFS_ROOT, conf);
            SparkOperatorResolver resolver = new SparkOperatorResolver(hdfsFileSystem, taskRunId);
            List<DataStore> inputs = resolver.resolveUpstreamDataStore(config);
            List<DataStore> outputs = resolver.resolveDownstreamDataStore(config);
            TaskAttemptReport taskAttemptReport = TaskAttemptReport.newBuilder()
                    .withInlets(inputs)
                    .withOutlets(outputs)
                    .build();
            report(taskAttemptReport);
        } catch (Throwable e) {
            logger.error("create hdfs file system failed", e);
        }
    }

}
