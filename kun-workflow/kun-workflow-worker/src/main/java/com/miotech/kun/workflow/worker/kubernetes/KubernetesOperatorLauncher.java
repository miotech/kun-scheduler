package com.miotech.kun.workflow.worker.kubernetes;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.FileAppender;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.miotech.kun.commons.utils.ExceptionUtils;
import com.miotech.kun.commons.utils.Props;
import com.miotech.kun.workflow.common.lineage.service.LineageService;
import com.miotech.kun.workflow.common.taskrun.dao.TaskRunDao;
import com.miotech.kun.workflow.core.execution.ExecCommand;
import com.miotech.kun.workflow.core.execution.KunOperator;
import com.miotech.kun.workflow.core.execution.OperatorContext;
import com.miotech.kun.workflow.core.execution.OperatorReport;
import com.miotech.kun.workflow.core.model.taskrun.TaskRun;
import com.miotech.kun.workflow.worker.JsonCodec;
import com.miotech.kun.workflow.worker.OperatorContextImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;

public class KubernetesOperatorLauncher {
    private static final Logger logger = LoggerFactory.getLogger(KubernetesOperatorLauncher.class);
    private volatile boolean cancelled = false;
    private volatile KunOperator operator;
    private static Props props;
    private volatile boolean finished = false;
    @Inject
    private TaskRunDao taskRunDao;
    @Inject
    private LineageService lineageService;


    public static void main(String args[]) {
        props = new Props(System.getenv());
        logger.info("props={}", props);
        ExecCommand command = readExecCommand();
        // 初始化logger
        initLogger(command.getLogPath());
        Injector injector = Guice.createInjector(
                new KubernetesWorkerModule(props)
        );
        KubernetesOperatorLauncher operatorLauncher = injector.getInstance(KubernetesOperatorLauncher.class);
        Thread exitHook = new Thread(() -> operatorLauncher.cancel());
        Runtime.getRuntime().addShutdownHook(exitHook);
        operatorLauncher.start(command);
    }

    private void start(ExecCommand command) {
        int exitCode = launchOperator(command) ? 0 : 1;
        finished = true;
        System.exit(exitCode);
    }

    private OperatorContext initContext(ExecCommand command) {
        return new OperatorContextImpl(command.getConfig(), command.getTaskRunId());
    }

    private static void initLogger(String logPath) {
        ch.qos.logback.classic.Logger rootLogger
                = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);

        LoggerContext context = rootLogger.getLoggerContext();

        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(context);
        encoder.setPattern("%d{HH:mm:ss.SSS} %-5level - %msg%n");
        encoder.start();

        FileAppender<ILoggingEvent> appender;
        appender = new FileAppender<>();
        appender.setContext(context);
        appender.setFile(trimPrefix(logPath));
        appender.setEncoder(encoder);
        appender.start();

        rootLogger.addAppender(appender);
    }

    private static String trimPrefix(String logPath) {
        if (!logPath.startsWith("file:")) {
            throw new IllegalArgumentException("Not a file resource: " + logPath);
        }
        return logPath.substring(5);
    }


    public synchronized void cancel() {
        if (finished) {
            return;
        }
        logger.info("Trying to cancel current operator.");
        if (cancelled) {
            logger.warn("Operator is already cancelled.");
            return;
        }

        cancelled = true;

        if (operator != null) {
            try {
                operator.abort();
            } catch (Exception e) {
                logger.error("Unexpected exception occurred during aborting operator.", e);
            }
        }
    }

    private boolean launchOperator(ExecCommand command) {
        Thread thread = Thread.currentThread();
        ClassLoader cl = thread.getContextClassLoader();
        try {
            // 加载Operator
            operator = loadOperator(command.getJarPath(), command.getClassName());
            operator.setContext(initContext(command));
            thread.setContextClassLoader(operator.getClass().getClassLoader());

            if (cancelled) {
                logger.info("Operator is cancelled, abort execution.");
            }

            // 初始化Operator
            logger.info("Initializing operator...");
            operator.init();
            logger.info("Operator has initialized successfully.");
            if (cancelled) {
                logger.info("Operator is cancelled, abort execution.");
                return false;
            }

            // 运行
            logger.info("Operator start running.");
            boolean success = operator.run();
            logger.info("Operator execution finished. success={}", success);

            if (cancelled) {
                logger.info("Operator is cancelled, abort execution.");
                return false;
            } else if (success) {
                if (operator.getReport().isPresent()) {
                    //process report
                    try {
                        OperatorReport operatorReport = new OperatorReport();
                        operatorReport.copyFromReport(operator.getReport().get());
                        processReport(command.getTaskRunId(), operatorReport);
                    } catch (Throwable e) {
                        logger.error("process operator report failed", e);
                    }
                }
                return true;
            } else {
                return false;
            }
        } catch (Throwable e) {
            logger.error("Unexpected exception occurred.", e);
            return false;
        } finally {
            thread.setContextClassLoader(cl);
        }
    }

    private KunOperator loadOperator(String jarPath, String mainClass) {
        try {
            URL[] urls = {new URL("jar:" + jarPath + "!/")};
            URLClassLoader cl = URLClassLoader.newInstance(urls, getClass().getClassLoader());
            Class<?> clazz = Class.forName(mainClass, true, cl);
            if (!KunOperator.class.isAssignableFrom(clazz)) {
                throw new IllegalArgumentException(mainClass + " is not a valid Operator class.");
            }
            return (KunOperator) clazz.newInstance();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load jar. jarPath=" + jarPath, e);
        }
    }


    private static ExecCommand readExecCommand() {
        String execCommandFilePath = props.getString("execCommandFile");
        try {
            File execCommandFile = new File(execCommandFilePath);
            ExecCommand execCommand = JsonCodec.MAPPER.readValue(execCommandFile, ExecCommand.class);
            logger.info("execCommand = {}", execCommand);
            //delete command file
            execCommandFile.delete();
            return execCommand;
        } catch (IOException e) {
            logger.error("failed to read exec command from file = {}", execCommandFilePath, e);
            throw ExceptionUtils.wrapIfChecked(e);
        }
    }


    private void processReport(Long taskRunId, OperatorReport report) {
        logger.debug("Update task's inlets/outlets. taskRunId={}, inlets={}, outlets={}",
                taskRunId, report.getInlets(), report.getOutlets());
        taskRunDao.updateTaskRunInletsOutlets(taskRunId,
                report.getInlets(), report.getOutlets());
        TaskRun taskRun = taskRunDao.fetchTaskRunById(taskRunId).get();
        lineageService.updateTaskLineage(taskRun.getTask(), report.getInlets(), report.getOutlets());

    }

}
