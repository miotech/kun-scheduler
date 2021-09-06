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
import com.miotech.kun.commons.rpc.RpcModule;
import com.miotech.kun.commons.rpc.RpcUtils;
import com.miotech.kun.commons.utils.Props;
import com.miotech.kun.workflow.common.lineage.service.LineageService;
import com.miotech.kun.workflow.common.taskrun.dao.TaskRunDao;
import com.miotech.kun.workflow.core.execution.*;
import com.miotech.kun.workflow.core.model.taskrun.TaskRun;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunStatus;
import com.miotech.kun.workflow.core.model.worker.DatabaseConfig;
import com.miotech.kun.workflow.facade.WorkflowExecutorFacade;
import com.miotech.kun.workflow.utils.DateTimeUtils;
import com.miotech.kun.workflow.worker.JsonCodec;
import com.miotech.kun.workflow.worker.OperatorContextImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

@Singleton
public class OperatorLauncher {

    private static final Logger logger = LoggerFactory.getLogger(OperatorLauncher.class);
    private volatile boolean cancelled = false;
    private volatile KunOperator operator;
    private volatile TaskRunStatus taskRunStatus = TaskRunStatus.RUNNING;
    private final int RPC_RETRY_TIME = 3;
    private final Long HEARTBEAT_INTERVAL = 5L;
    private WorkflowExecutorFacade executorFacade;
    private Long workerId;
    private static Props props;

    @Inject
    private TaskRunDao taskRunDao;
    @Inject
    private LineageService lineageService;


    private ArrayBlockingQueue<TaskAttemptMsg> statusUpdateQueue = new ArrayBlockingQueue<>(5);


    private static Injector injector;

    @Inject
    public OperatorLauncher(WorkflowExecutorFacade executorFacade) {
        this.executorFacade = executorFacade;
    }

    public static void main(String args[]) {
        String in = args[0];
        String config = args[1];
        String out = args[2];
        ExecCommand command = readCommand(in);
        // 初始化logger
        initLogger(command.getLogPath());
        DatabaseConfig databaseConfig = readConfig(config);
        props = initProps(databaseConfig);
        props.put("rpc.registry", command.getRegisterUrl());
        Integer workerPort = RpcUtils.getRandomPort();
        props.put("rpc.port", workerPort);
        injector = Guice.createInjector(
                new RpcModule(props),
                new LocalWorkerModule(props)
        );
        Thread mainThread = Thread.currentThread();
        OperatorLauncher operatorLauncher = injector.getInstance(OperatorLauncher.class);
        Thread exitHook = new Thread(() -> operatorLauncher.cancel(mainThread));
        Runtime.getRuntime().addShutdownHook(exitHook);
        operatorLauncher.start(command, out);
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
        return props;
    }

    private void start(ExecCommand command, String out) {
        workerId = getPid();
        Thread statusUpdateTask = new Thread(new StatusUpdateTask());
        statusUpdateTask.start();
        TaskAttemptMsg msg = launchOperator(command);
        try {
            logger.debug("wait statusUpdate message send to executor");
            statusUpdateTask.join();
        } catch (InterruptedException e) {
            logger.debug("wait statusUpdate message send to executor failed", e);
        }
        writeResult(out, msg);
    }


    private synchronized void cancel(Thread mainThread) {
        if (taskRunStatus.isFinished()) {
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
                mainThread.join();
            } catch (Exception e) {
                logger.error("Unexpected exception occurred during aborting operator.", e);
            }
        }
    }

    //rpc
    public TaskAttemptMsg launchOperator(ExecCommand command) {
        TaskAttemptMsg msg = new TaskAttemptMsg();
        msg.setWorkerId(workerId);
        msg.setTaskRunId(command.getTaskRunId());
        msg.setTaskAttemptId(command.getTaskAttemptId());
        msg.setStartAt(DateTimeUtils.now());
        msg.setQueueName(command.getQueueName());
        TaskAttemptMsg runningMsg = msg.copy();
        taskRunStatus = TaskRunStatus.RUNNING;
        runningMsg.setTaskRunStatus(TaskRunStatus.RUNNING);
        statusUpdate(runningMsg);
        Thread thread = Thread.currentThread();
        ClassLoader cl = thread.getContextClassLoader();
        try {
            // 加载Operator
            operator = loadOperator(command.getJarPath(), command.getClassName());
            operator.setContext(initContext(command));
            thread.setContextClassLoader(operator.getClass().getClassLoader());

            if (cancelled) {
                logger.info("Operator is cancelled, abort execution.");
                TaskAttemptMsg cancelledMsg = setCancelledMsg(msg);
                statusUpdate(cancelledMsg);
                return cancelledMsg;
            }

            // 初始化Operator
            logger.info("Initializing operator...");
            operator.init();
            logger.info("Operator has initialized successfully.");
            if (cancelled) {
                TaskAttemptMsg cancelledMsg = setCancelledMsg(msg);
                statusUpdate(cancelledMsg);
                logger.info("Operator is cancelled, abort execution.");
                return cancelledMsg;
            }

            // 运行
            logger.info("Operator start running.");
            boolean success = operator.run();
            logger.info("Operator execution finished. success={}", success);

            if (cancelled) {
                TaskAttemptMsg cancelledMsg = setCancelledMsg(msg);
                statusUpdate(cancelledMsg);
                logger.info("Operator is cancelled, abort execution.");
                return cancelledMsg;
            } else if (success) {
                TaskAttemptMsg successMessage = msg.copy();
                taskRunStatus = TaskRunStatus.SUCCESS;
                successMessage.setTaskRunStatus(TaskRunStatus.SUCCESS);
                successMessage.setEndAt(DateTimeUtils.now());
                if (operator.getReport().isPresent()) {
                    OperatorReport operatorReport = new OperatorReport();
                    operatorReport.copyFromReport(operator.getReport().get());
                    successMessage.setOperatorReport(operatorReport);
                    try {
                        processReport(command.getTaskRunId(), operatorReport);
                    } catch (Throwable e) {
                        logger.error("process operator report failed", e);
                    }
                } else {
                    successMessage.setOperatorReport(OperatorReport.BLANK);
                }
                statusUpdate(successMessage);
                return successMessage;
            } else {
                TaskAttemptMsg failedMsg = setFailedMsg(msg);
                statusUpdate(failedMsg);
                return failedMsg;
            }
        } catch (Throwable e) {
            logger.error("Unexpected exception occurred.", e);
            TaskAttemptMsg failedMsg = setFailedMsg(msg);
            statusUpdate(failedMsg);
            return failedMsg;
        } finally {
            thread.setContextClassLoader(cl);
        }
    }

    private void statusUpdate(TaskAttemptMsg msg) {
        statusUpdateQueue.add(msg);
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

    private void writeResult(String outputFile, TaskAttemptMsg result) {
        try {
            JsonCodec.MAPPER.writeValue(new File(outputFile), result);
        } catch (IOException e) {
            logger.error("Failed to write output. path={}", outputFile, e);
        }
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

    private TaskAttemptMsg setCancelledMsg(TaskAttemptMsg msg) {
        taskRunStatus = TaskRunStatus.ABORTED;
        TaskAttemptMsg cancelledMsg = msg.copy();
        cancelledMsg.setTaskRunStatus(TaskRunStatus.ABORTED);
        cancelledMsg.setEndAt(DateTimeUtils.now());
        if (operator.getReport().isPresent()) {
            OperatorReport operatorReport = new OperatorReport();
            operatorReport.copyFromReport(operator.getReport().get());
            cancelledMsg.setOperatorReport(operatorReport);
        } else {
            cancelledMsg.setOperatorReport(OperatorReport.BLANK);
        }
        return cancelledMsg;
    }

    private TaskAttemptMsg setFailedMsg(TaskAttemptMsg msg) {
        taskRunStatus = TaskRunStatus.FAILED;
        TaskAttemptMsg failedMsg = msg.copy();
        failedMsg.setTaskRunStatus(TaskRunStatus.FAILED);
        failedMsg.setEndAt(DateTimeUtils.now());
        if (operator != null && operator.getReport().isPresent()) {
            OperatorReport operatorReport = new OperatorReport();
            operatorReport.copyFromReport(operator.getReport().get());
            failedMsg.setOperatorReport(operatorReport);
        } else {
            failedMsg.setOperatorReport(OperatorReport.BLANK);
        }
        return failedMsg;
    }


    private Long getPid() {
        String name = ManagementFactory.getRuntimeMXBean().getName();
        String pid = name.split("@")[0];
        return Long.valueOf(pid);
    }

    private void processReport(Long taskRunId, OperatorReport report) {
        logger.debug("Update task's inlets/outlets. taskRunId={}, inlets={}, outlets={}",
                taskRunId, report.getInlets(), report.getOutlets());
        taskRunDao.updateTaskRunInletsOutlets(taskRunId,
                report.getInlets(), report.getOutlets());
        TaskRun taskRun = taskRunDao.fetchTaskRunById(taskRunId).get();
        lineageService.updateTaskLineage(taskRun.getTask(), report.getInlets(), report.getOutlets());

    }

    class StatusUpdateTask implements Runnable {

        @Override
        public void run() {
            while (!cancelled) {
                try {
                    TaskAttemptMsg msg = statusUpdateQueue.take();
                    int sendTime = 0;
                    boolean updateSuccess = false;
                    while (!updateSuccess && sendTime <= RPC_RETRY_TIME) {
                        try {
                            if (sendTime > 0) {
                                Uninterruptibles.sleepUninterruptibly(HEARTBEAT_INTERVAL, TimeUnit.SECONDS);
                            }
                            updateSuccess = executorFacade.statusUpdate(msg);
                            logger.info("send update task status message = {}, to executor result = {}", msg, updateSuccess);
                        } catch (Exception e) {
                            sendTime++;
                            logger.error("send update task status message = {}, to executor failed,retry = {}", msg, sendTime, e);
                        }
                    }
                    if (msg.getTaskRunStatus().isFinished()) {
                        cancelled = true;
                    }
                } catch (InterruptedException e) {
                    logger.error("take TaskAttemptMsg from queue failed", e);
                }
            }

        }
    }
}
