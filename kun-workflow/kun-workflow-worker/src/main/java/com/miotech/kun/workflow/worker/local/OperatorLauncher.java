package com.miotech.kun.workflow.worker.local;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.FileAppender;
import com.google.common.util.concurrent.Uninterruptibles;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.miotech.kun.commons.utils.Props;
import com.miotech.kun.workflow.common.lineage.service.LineageService;
import com.miotech.kun.workflow.common.taskrun.dao.TaskRunDao;
import com.miotech.kun.workflow.core.execution.ExecCommand;
import com.miotech.kun.workflow.core.execution.KunOperator;
import com.miotech.kun.workflow.core.execution.OperatorContext;
import com.miotech.kun.workflow.core.execution.OperatorReport;
import com.miotech.kun.workflow.core.model.taskrun.TaskRun;
import com.miotech.kun.workflow.core.model.worker.DatabaseConfig;
import com.miotech.kun.workflow.worker.JsonCodec;
import com.miotech.kun.workflow.worker.OperatorContextImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.time.OffsetDateTime;
import java.util.concurrent.TimeUnit;

@Singleton
public class OperatorLauncher {

    private static final Logger logger = LoggerFactory.getLogger(OperatorLauncher.class);
    private volatile boolean cancelled = false;
    private volatile KunOperator operator;
    private static Props props;
    private volatile boolean finished = false;
    private final Integer SUCCESS_CODE = 0;
    private final Integer FAILED_CODE = 1;
    private final Integer ABORTED_CODE = 2;

    @Inject
    private TaskRunDao taskRunDao;
    @Inject
    private LineageService lineageService;


    private static Injector injector;

    public static void main(String args[]) {
        String in = args[0];
        String config = args[1];
        ExecCommand command = readCommand(in);
        // 初始化logger
        initLogger(command.getLogPath());
        DatabaseConfig databaseConfig = readConfig(config);
        props = initProps(databaseConfig);
        injector = Guice.createInjector(
                new LocalWorkerModule(props)
        );
        OperatorLauncher operatorLauncher = injector.getInstance(OperatorLauncher.class);
        Thread exitHook = new Thread(() -> {
            operatorLauncher.cancel();
        });

        Runtime.getRuntime().addShutdownHook(exitHook);
        operatorLauncher.start(command);
    }

    private static Props initProps(DatabaseConfig databaseConfig) {
        Props props = new Props();
        if (databaseConfig.getDatasourceUrl() != null) {
            props.put("datasource.jdbcUrl", databaseConfig.getDatasourceUrl());
        }
        if (databaseConfig.getDatasourceUser() != null) {
            props.put("datasource.username", databaseConfig.getDatasourceUser());
        }
        if (databaseConfig.getDatasourcePassword() != null) {
            props.put("datasource.password", databaseConfig.getDatasourcePassword());
        }
        if (databaseConfig.getDatasourceDriver() != null) {
            props.put("datasource.driverClassName", databaseConfig.getDatasourceDriver());
        }
        if (databaseConfig.getDatasourceMaxPoolSize() != null) {
            props.put("datasource.maxPoolSize", databaseConfig.getDatasourceMaxPoolSize());
        }
        if (databaseConfig.getDatasourceMinIdle() != null) {
            props.put("datasource.minimumIdle", databaseConfig.getDatasourceMinIdle());
        }
        if (databaseConfig.getNeo4juri() != null) {
            props.put("neo4j.uri", databaseConfig.getNeo4juri());
        }
        if (databaseConfig.getNeo4jUser() != null) {
            props.put("neo4j.username", databaseConfig.getNeo4jUser());
        }
        if (databaseConfig.getNeo4jPassword() != null) {
            props.put("neo4j.password", databaseConfig.getNeo4jPassword());
        }
        if (databaseConfig.getExecutorRpcHost() != null) {
            props.put("executorRpcHost", databaseConfig.getExecutorRpcHost());
        }
        if (databaseConfig.getExecutorRpcPort() != null) {
            props.put("executorRpcPort", databaseConfig.getExecutorRpcPort());
        }
        return props;
    }

    private void start(ExecCommand command) {
        int exitCode = launchOperator(command);
        finished = true;
        System.exit(exitCode);
    }

    private void cancel() {
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
                logger.info("run operator abort...");
                operator.abort();
                waitFinished();
            } catch (Exception e) {
                logger.error("Unexpected exception occurred during aborting operator.", e);
            }
        }
    }

    public int launchOperator(ExecCommand command) {
        Thread thread = Thread.currentThread();
        ClassLoader cl = thread.getContextClassLoader();
        try {
            // 加载Operator
            operator = loadOperator(command.getJarPath(), command.getClassName());
            operator.setContext(initContext(command));
            thread.setContextClassLoader(operator.getClass().getClassLoader());

            if (cancelled) {
                logger.info("Operator is cancelled, abort execution.");
                return ABORTED_CODE;
            }

            // 初始化Operator
            logger.info("Initializing operator...");
            operator.init();
            logger.info("Operator has initialized successfully.");
            if (cancelled) {
                logger.info("Operator is cancelled, abort execution.");
                return ABORTED_CODE;
            }

            // 运行
            logger.info("Operator start running.");
            boolean success = operator.run();
            logger.info("Operator execution finished. success={}", success);

            if (cancelled) {
                logger.info("Operator is cancelled, abort execution.");
                return ABORTED_CODE;
            } else if (success) {
                if (operator.getReport().isPresent()) {
                    try {
                        OperatorReport operatorReport = new OperatorReport();
                        operatorReport.copyFromReport(operator.getReport().get());
                        processReport(command.getTaskRunId(), operatorReport);
                    } catch (Throwable e) {
                        logger.error("process operator report failed", e);
                    }
                }
                return SUCCESS_CODE;
            } else {
                return FAILED_CODE;
            }
        } catch (Throwable e) {
            logger.error("Unexpected exception occurred.", e);
            return FAILED_CODE;
        } finally {
            thread.setContextClassLoader(cl);
        }
    }

    private void waitFinished() {
        while (!finished) {
            Uninterruptibles.sleepUninterruptibly(100, TimeUnit.MILLISECONDS);
        }
    }

    private static ExecCommand readCommand(String inputFile) {
        try {
            return JsonCodec.MAPPER.readValue(new File(inputFile), ExecCommand.class);
        } catch (IOException e) {
            logger.error("Failed to read input. path={}", inputFile, e);
            throw new IllegalStateException(e);
        }
    }

    private static DatabaseConfig readConfig(String configFile) {
        try {
            return JsonCodec.MAPPER.readValue(new File(configFile), DatabaseConfig.class);
        } catch (IOException e) {
            logger.error("Failed to read config. path={}", configFile, e);
            throw new IllegalStateException(e);
        }
    }

    private OperatorContext initContext(ExecCommand command) {
        OperatorContext context = new OperatorContextImpl(command.getConfig(), command.getTaskRunId(),command.getExecuteTarget());
        injector.injectMembers(context);
        return context;
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

        //remove all exist appender
        ch.qos.logback.classic.Logger workerLogger
                = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger("com.miotech.kun.workflow");
        workerLogger.detachAndStopAllAppenders();
        rootLogger.detachAndStopAllAppenders();
        rootLogger.setLevel(Level.INFO);

        rootLogger.addAppender(appender);
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

    private static String trimPrefix(String logPath) {
        if (!logPath.startsWith("file:")) {
            throw new IllegalArgumentException("Not a file resource: " + logPath);
        }
        return logPath.substring(5);
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
