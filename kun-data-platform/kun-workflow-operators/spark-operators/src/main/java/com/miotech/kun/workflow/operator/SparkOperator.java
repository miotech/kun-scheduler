package com.miotech.kun.workflow.operator;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.miotech.kun.commons.utils.IdGenerator;
import com.miotech.kun.workflow.core.execution.*;
import com.miotech.kun.workflow.operator.spark.models.AppInfo;
import com.miotech.kun.workflow.operator.spark.models.SparkJob;
import com.miotech.kun.workflow.operator.spark.models.LivyStateInfo;
import com.miotech.kun.workflow.utils.JSONUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.miotech.kun.workflow.operator.SparkConfiguration.*;

public class SparkOperator extends LivyBaseSparkOperator {
    private static final Logger logger = LoggerFactory.getLogger(SparkOperator.class);

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

        String configLineageOutputPath = SparkConfiguration.getString(context, CONF_LINEAGE_OUTPUT_PATH);
        String configLineageJarPath = SparkConfiguration.getString(context, CONF_LINEAGE_JAR_PATH);
        String configS3AccessKey = SparkConfiguration.getString(context, CONF_S3_ACCESS_KEY);
        String configS3SecretKey = SparkConfiguration.getString(context, CONF_S3_SECRET_KEY);

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

        List<String> allJars = new ArrayList<>();
        if (!Strings.isNullOrEmpty(jars)) {
            allJars.addAll(Arrays.asList(jars.split(",")));
        }
        if(!Strings.isNullOrEmpty(configLineageJarPath)){
            allJars.add(configLineageJarPath);
            job.addConf("spark.sql.queryExecutionListeners","za.co.absa.spline.harvester.listener.SplineQueryExecutionListener");
        }
        job.setJars(allJars);

        // lineage config
        if(!Strings.isNullOrEmpty(configLineageOutputPath)){
            job.addConf("spark.hadoop.spline.hdfs_dispatcher.address", configLineageOutputPath);
        }
        if(!Strings.isNullOrEmpty(configS3AccessKey)){
            job.addConf("spark.fs.s3a.access.key", configS3AccessKey);
        }
        if(!Strings.isNullOrEmpty(configS3SecretKey)){
            job.addConf("spark.fs.s3a.secret.key", configS3SecretKey);
        }
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

        LivyStateInfo.State jobState = null;
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
        if (jobState.equals(LivyStateInfo.State.SUCCESS)) {
            //wait spline send execPlan
            waitForSeconds(15);
            //解析spark 任务上下游
            TaskAttemptReport taskAttemptReport = SparkQueryPlanLineageAnalyzer.lineageAnalysis(context.getConfig(), taskRunId);
            if(taskAttemptReport != null)
                report(taskAttemptReport);
            return true;
        } else {
            return false;
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
                .define(CONF_VARIABLES, ConfigDef.Type.STRING, "{}", true, "Spark arguments and configuration variables, use like `--param1 ${a}`, supply with {\"a\": \"b\"}", CONF_VARIABLES)
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
