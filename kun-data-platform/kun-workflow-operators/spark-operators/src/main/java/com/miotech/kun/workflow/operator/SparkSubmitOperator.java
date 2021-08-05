package com.miotech.kun.workflow.operator;

import com.miotech.kun.commons.utils.StringUtils;
import com.miotech.kun.workflow.core.execution.*;
import com.miotech.kun.workflow.operator.spark.clients.SparkClient;
import com.miotech.kun.workflow.operator.spark.clients.YarnLoggerParser;
import com.miotech.kun.workflow.operator.spark.models.Application;
import com.miotech.kun.workflow.operator.spark.models.YarnStateInfo;
import com.miotech.kun.workflow.utils.JSONUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.StartedProcess;
import org.zeroturnaround.exec.stream.slf4j.Slf4jStream;
import org.zeroturnaround.process.JavaProcess;
import org.zeroturnaround.process.ProcessUtil;
import org.zeroturnaround.process.Processes;

import java.io.*;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.miotech.kun.workflow.operator.SparkConfiguration.*;

public class SparkSubmitOperator extends KunOperator {

    private static final Logger logger = LoggerFactory.getLogger(SparkSubmitOperator.class);
    private Process process;
    private  Boolean submitted = false;
    private SparkClient sparkClient;
    private String appId;
    private final YarnLoggerParser loggerParser = new YarnLoggerParser();
    private String yarnHost;
    private String deployMode;
    private String master;

    private static final String VARIABLES = "variables";
    private static final String DISPLAY_COMMAND = "spark command";
    private static final Long FORCES_WAIT_SECONDS_DEFAULT_VALUE = 10l;
    private static final int HTTP_TIMEOUT_LIMIT = 10;

    @Override
    public void init() {
        OperatorContext context = getContext();
        logger.info("Recieved task config: {}", JSONUtils.toJsonString(context.getConfig()));

        yarnHost = SparkConfiguration.getString(context, SparkConfiguration.SPARK_YARN_HOST);
        deployMode = SparkConfiguration.getString(context, SPARK_DEPLOY_MODE);
        master = SparkConfiguration.getString(context, SPARK_MASTER);
        sparkClient = new SparkClient(yarnHost);
    }


    @Override
    public boolean run() {
        OperatorContext context = getContext();
        Config config = context.getConfig();
        Long taskRunId = context.getTaskRunId();
        Map<String, String> variables = JSONUtils.jsonStringToStringMap(config.getString(VARIABLES));

        //build shell cmd
        String baseCommand = config.getString(SPARK_BASE_COMMAND);
        String params = config.getString(SPARK_SUBMIT_PARMAS);

//        Map<String, String> runTimeParams = new HashMap<>();
//        runTimeParams.put("spark.hadoop.taskRunId", taskRunId.toString());
        //TODO: refactor spark cmd building
        String runTimeParams = " --conf spark.hadoop.taskRunId=" + taskRunId.toString() + " ";

        String fullCommand = StringUtils.resolveWithVariable(baseCommand + runTimeParams + params, variables);

        logger.debug("Execute command:\n\n {}", fullCommand);

        try {
            File commandFile = File.createTempFile("spark-submit-operator-", ".sh");
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(commandFile))) {
                writer.write(fullCommand);
            }
            ProcessExecutor processExecutor = new ProcessExecutor();
            OutputStream stderrStream = new ByteArrayOutputStream();
            StartedProcess startedProcess = processExecutor
                    .command("sh", commandFile.getPath())
                    .redirectOutput(Slf4jStream.of(logger).asInfo())
                    .redirectError(stderrStream)
                    .redirectErrorAlsoTo(Slf4jStream.of(logger).asInfo())
                    .start();
            process = startedProcess.getProcess();

            // wait for termination
            int exitCode = startedProcess.getFuture().get().getExitValue();
            submitted = true;
            if(exitCode != 0){
                return false;
            }

            boolean finalStatus = true;
            //if cluster mode, parse application Id from output, track yarn app status
            if("yarn".equalsIgnoreCase(master) && "clsuter".equalsIgnoreCase(deployMode)){
                String stderrString = stderrStream.toString();
                Pattern applicationIdPattern = Pattern.compile(".*(application_\\d{13}_\\d{4}).*");
                final Matcher matcher = applicationIdPattern.matcher(stderrString);
                if (matcher.matches()){
                    appId = matcher.group(1);
                    logger.info("Yarn ApplicationId: {}", appId);
                    finalStatus = trackYarnAppStatus(appId);
                }else {
                    throw new IllegalStateException("Yarn applicationId not found");
                }
            }

            if(finalStatus){
                try{
                    waitForSeconds(10);
                    TaskAttemptReport taskAttemptReport = SparkQueryPlanLineageAnalyzer.lineageAnalysis(context.getConfig(), taskRunId);
                    if (taskAttemptReport != null)
                        report(taskAttemptReport);
                }catch (Exception e){
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
        if(!submitted){
            Long waitSeconds = getContext().getConfig().getLong("forceWaitSeconds");
            JavaProcess javaProcess = Processes.newJavaProcess(process);
            if (javaProcess.isAlive()) {
                try {
                    ProcessUtil.destroyGracefullyOrForcefullyAndWait(javaProcess, 30, TimeUnit.SECONDS, waitSeconds, TimeUnit.SECONDS);
                    logger.info("Process is successfully terminated");
                } catch (IOException | TimeoutException e) {
                    logger.error("{}", e);
                } catch (InterruptedException e) {
                    logger.error("{}", e);
                    Thread.currentThread().interrupt();
                }
            } else {
                logger.info("Process already finished");
            }
        }else{
            //TODO: kill spark app in yarn/k8/mesos
            sparkClient.killApplication(appId);
        }

    }

    @Override
    public ConfigDef config() {
        return new ConfigDef()
                .define(SPARK_BASE_COMMAND, ConfigDef.Type.STRING, true, "base cmd: spark-submit/spark-sql", SPARK_BASE_COMMAND)
                .define(SPARK_SUBMIT_PARMAS, ConfigDef.Type.STRING, true, "spark-submit/spark-sql parmas", SPARK_SUBMIT_PARMAS)
                .define(SPARK_YARN_HOST, ConfigDef.Type.STRING, "", true, "Yarn host to submit application, in the format `ip:port`", SPARK_YARN_HOST)
                .define(SPARK_DEPLOY_MODE, ConfigDef.Type.STRING, "", true, "deploy mode", SPARK_DEPLOY_MODE)
                .define(CONF_LINEAGE_OUTPUT_PATH, ConfigDef.Type.STRING, CONF_LINEAGE_OUTPUT_PATH_VALUE_DEFAULT, true, "file system address to store lineage analysis report, in the format `s3a://BUCKET/path` or `hdfs://host:port/path`", CONF_LINEAGE_OUTPUT_PATH)
                .define(CONF_LINEAGE_JAR_PATH, ConfigDef.Type.STRING, CONF_LINEAGE_JAR_PATH_VALUE_DEFAULT, true, "the jar used for lineage analysis, in the format `s3a://BUCKET/xxx/xxx.jar` or `hdfs://host:port/xxx/xxx.jar`", CONF_LINEAGE_JAR_PATH)
                .define(VAR_S3_ACCESS_KEY, ConfigDef.Type.STRING, CONF_S3_ACCESS_KEY_VALUE_DEFAULT, true, "if using s3 to store lineage analysis report, need s3 credentials", VAR_S3_ACCESS_KEY)
                .define(VAR_S3_SECRET_KEY, ConfigDef.Type.STRING, CONF_S3_SECRET_KEY_VALUE_DEFAULT, true, "if using s3 to store lineage analysis report, need s3 credentials", VAR_S3_SECRET_KEY)
                .define("forceWaitSeconds", ConfigDef.Type.LONG, FORCES_WAIT_SECONDS_DEFAULT_VALUE, true, "force terminate wait seconds", "forceWaitSeconds")
                .define(VARIABLES, ConfigDef.Type.STRING, "{}", true, "bash variables", "variables");
    }

    @Override
    public Resolver getResolver() {
        // TODO: implement this
        return new NopResolver();
    }

    public boolean trackYarnAppStatus(String appId){
        int timeout = 0;
        YarnStateInfo.State jobState = null;
        Application sparkApp = null;
        do {
            try {
                sparkApp = sparkClient.getApp(appId);
                jobState = YarnStateInfo.State.valueOf(sparkApp.getFinalStatus());
                timeout = 0;
            } catch (RuntimeException e) {
                timeout++;
                logger.warn("get job information from yarn timeout, times = {}", timeout);
                if (timeout >= HTTP_TIMEOUT_LIMIT) {
                    logger.error("get spark job information from yarn failed", e);
                    throw e;
                }
            }
            if (jobState == null) {
                throw new IllegalStateException("Cannot find state for job: " + appId);
            }
            waitForSeconds(5);
        } while ( !jobState.isFinished());
        tailingYarnLog(sparkApp.getAmContainerLogs());
        if(jobState.isSuccess()){
            return true;
        }else{
            return false;
        }
    }

    private void waitForSeconds(int seconds) {
        try {
            Thread.sleep(seconds * 1000);
        } catch (InterruptedException e) {
            logger.error("Failed in wait for : {}s", seconds, e);
            Thread.currentThread().interrupt();
        }
    }

    private void tailingYarnLog(String logUrl) {
        try {
            logger.info("Fetch log from {}", logUrl);
            logger.info(loggerParser.getYarnLogs(logUrl));
        } catch (Exception e) {
            logger.error("Error in fetch application logs, {}", e);
        }
    }

}
