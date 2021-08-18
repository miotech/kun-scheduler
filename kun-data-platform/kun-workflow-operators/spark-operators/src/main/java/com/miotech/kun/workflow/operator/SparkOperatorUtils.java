package com.miotech.kun.workflow.operator;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Strings;
import com.miotech.kun.workflow.operator.spark.clients.SparkClient;
import com.miotech.kun.workflow.operator.spark.models.Application;
import com.miotech.kun.workflow.operator.spark.models.YarnStateInfo;
import org.slf4j.Logger;
import com.miotech.kun.workflow.operator.spark.clients.YarnLoggerParser;
import org.zeroturnaround.process.JavaProcess;
import org.zeroturnaround.process.ProcessUtil;
import org.zeroturnaround.process.Processes;


public class SparkOperatorUtils {
    private static final int HTTP_TIMEOUT_LIMIT = 10;

    public static void waitForSeconds(int seconds) {
        try {
            Thread.sleep(seconds * 1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static void tailingYarnLog(String logUrl, Logger logger, YarnLoggerParser loggerParser) {
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
            params.add("--" + entry.getKey());
            params.add(entry.getValue());
        }
        return params;
    }

    public static List<String> parseSparkConf(Map<String, String> map) {
        List<String> params = new ArrayList<>();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            params.add("--conf");
            params.add(entry.getKey() + "=" + entry.getValue());
        }
        return params;
    }

    public static String parseYarnAppId(OutputStream stderrStream) {

        String stderrString = stderrStream.toString();
        Pattern applicationIdPattern = Pattern.compile(".*(application_\\d{13}_\\d{4}).*");
        final Matcher matcher = applicationIdPattern.matcher(stderrString);
        if (matcher.matches()) {
            String appId = matcher.group(1);
            return appId;
        }
        return null;
    }

    public static boolean trackYarnAppStatus(String appId, SparkClient sparkClient, Logger logger, YarnLoggerParser loggerParser) {
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
            SparkOperatorUtils.waitForSeconds(5);
        } while (!jobState.isFinished());
        SparkOperatorUtils.tailingYarnLog(sparkApp.getAmContainerLogs(), logger, loggerParser);
        if (jobState.isSuccess()) {
            return true;
        } else {
            return false;
        }
    }

    public static void abortSparkJob(String appId, Logger logger, SparkClient sparkClient, Process process) {
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
}
