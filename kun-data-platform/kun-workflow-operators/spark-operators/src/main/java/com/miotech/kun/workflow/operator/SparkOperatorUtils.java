package com.miotech.kun.workflow.operator;

import com.google.common.base.Strings;
import com.miotech.kun.workflow.core.execution.OperatorContext;
import com.miotech.kun.workflow.operator.spark.clients.SparkClient;
import com.miotech.kun.workflow.operator.spark.clients.YarnLoggerParser;
import com.miotech.kun.workflow.operator.spark.models.Application;
import com.miotech.kun.workflow.operator.spark.models.YarnStateInfo;
import org.slf4j.Logger;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.StartedProcess;
import org.zeroturnaround.exec.stream.slf4j.Slf4jStream;
import org.zeroturnaround.process.JavaProcess;
import org.zeroturnaround.process.ProcessUtil;
import org.zeroturnaround.process.Processes;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.miotech.kun.workflow.operator.SparkConfiguration.*;


public class SparkOperatorUtils {
    private static final int HTTP_TIMEOUT_LIMIT = 10;
    private String appId;
    private String yarnHost;
    private Process process;
    private SparkClient sparkClient;
    private OutputStream stderrStream = new ByteArrayOutputStream(1024 * 1024 * 10);
    private static Logger logger;
    private final YarnLoggerParser loggerParser = new YarnLoggerParser();
    private OperatorContext context;

    public void init(OperatorContext context, Logger logger) {
        this.context = context;
        this.logger = logger;

        yarnHost = SparkConfiguration.getString(context, SparkConfiguration.SPARK_YARN_HOST);
        if (Strings.isNullOrEmpty(yarnHost)) {
            String livyHost = SparkConfiguration.getString(context, CONF_LIVY_HOST);
            yarnHost = String.join(":", Arrays.copyOf(livyHost.split(":"), livyHost.split(":").length - 1));
        }
        sparkClient = new SparkClient(yarnHost);
    }

    public static void waitForSeconds(int seconds) {
        try {
            Thread.sleep(seconds * 1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void tailingYarnLog(String logUrl) {
        try {
            logger.info("Fetch log from {}", logUrl);
            logger.info(loggerParser.getYarnLogs(logUrl));
        } catch (Exception e) {
            logger.error("Error in fetch application logs, {}", e);
        }
    }

    public static List<String> parseSparkSubmitParmas(Map<String, String> map) {
        List<String> params = new ArrayList<>();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            params.add("--" + entry.getKey().trim());
            params.add(entry.getValue().trim());
        }
        return params;
    }

    public static List<String> parseSparkConf(Map<String, String> map) {
        List<String> params = new ArrayList<>();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            params.add("--conf");
            params.add(entry.getKey().trim() + "=" + entry.getValue().trim());
        }
        return params;
    }

    public String parseYarnAppId() {

        String stderrString = stderrStream.toString();
        Pattern applicationIdPattern = Pattern.compile(".* (application_\\d+_\\d+) .*");
        for (String line : stderrString.split("\n")) {
            final Matcher matcher = applicationIdPattern.matcher(line);
            if (matcher.matches()) {
                String appId = matcher.group(1).trim();
                logger.info("yarn application ID: {}", appId);
                return appId;
            }
        }
        return null;
    }

    public boolean trackYarnAppStatus(String appId) {
        int timeout = 0;
        YarnStateInfo.State jobState = null;
        Application sparkApp = null;
        do {
            try {
                sparkApp = sparkClient.getApp(appId);
                jobState = YarnStateInfo.State.valueOf(sparkApp.getFinalStatus());
                timeout = 0;
            } catch (Exception e) {
                timeout++;
                logger.warn("get job information from yarn timeout, times = {}", timeout, e);
                if (timeout >= HTTP_TIMEOUT_LIMIT) {
                    logger.error("get spark job information from yarn failed", e);
                    return false;
                }
            }
            SparkOperatorUtils.waitForSeconds(5);
        } while (!jobState.isFinished());
        tailingYarnLog(sparkApp.getAmContainerLogs());
        if (jobState.isSuccess()) {
            return true;
        } else {
            return false;
        }
    }

    public void abortSparkJob(String appId) {
        if (Strings.isNullOrEmpty(appId)) {
            logger.info("aborting un-submitted process");
            JavaProcess javaProcess = Processes.newJavaProcess(process);
            if (javaProcess.isAlive()) {
                try {
                    ProcessUtil.destroyGracefullyOrForcefullyAndWait(javaProcess, 30, TimeUnit.SECONDS, 10, TimeUnit.SECONDS);
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
        } else {
            //TODO: kill spark app in yarn/k8/mesos
            logger.info("aborting application in YARN: " + appId);
            sparkClient.killApplication(appId);
        }
    }

    public boolean execSparkSubmitCmd(List<String> cmd) {
        try {
            ProcessExecutor processExecutor = new ProcessExecutor();

            StartedProcess startedProcess = processExecutor
                    .environment(VAR_S3_ACCESS_KEY, context.getConfig().getString(CONF_S3_ACCESS_KEY))
                    .environment(VAR_S3_SECRET_KEY, context.getConfig().getString(CONF_S3_SECRET_KEY))
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
            appId = parseYarnAppId();
            logger.info("yarn application ID: {}", appId);

            if (!Strings.isNullOrEmpty(appId)) {
                finalStatus = trackYarnAppStatus(appId);
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

    public void abort() {
        appId = parseYarnAppId();
        abortSparkJob(appId);
    }
}
