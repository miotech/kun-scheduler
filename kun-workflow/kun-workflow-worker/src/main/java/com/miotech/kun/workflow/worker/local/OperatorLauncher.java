package com.miotech.kun.workflow.worker.local;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.FileAppender;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.miotech.kun.commons.rpc.RpcModule;
import com.miotech.kun.commons.rpc.RpcUtils;
import com.miotech.kun.commons.utils.Props;
import com.miotech.kun.workflow.core.execution.*;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunStatus;
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

@Singleton
public class OperatorLauncher {

    private static final Logger logger = LoggerFactory.getLogger(OperatorLauncher.class);
    private volatile boolean cancelled = false;
    private volatile KunOperator operator;
    private volatile boolean abort = false;
    private volatile TaskRunStatus taskRunStatus = TaskRunStatus.INITIALIZING;
    private final int RPC_RETRY_TIME = 10;
    private final Long HEARTBEAT_INTERVAL = 5 * 1000L;
    private WorkflowExecutorFacade executorFacade;
    private InitService initService;
    private Long workerId;
    private static Props props;

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
        HeartBeatMessage heartBeatMessage = readConfig(config);
        props = new Props();
        props.put("rpc.registry", command.getRegisterUrl());
        Integer workerPort = RpcUtils.getRandomPort();
        props.put("rpc.port", workerPort);
        injector = Guice.createInjector(
                new RpcModule(props),
                new WorkerModule(props)
        );
        OperatorLauncher operatorLauncher = injector.getInstance(OperatorLauncher.class);
        operatorLauncher.start(command, heartBeatMessage, out);
    }

    private void start(ExecCommand command, HeartBeatMessage heartBeatMessage, String out) {
        if (heartBeatMessage.getWorkerId() == null) {
            heartBeatMessage.setWorkerId(getPid());
        }
        workerId = heartBeatMessage.getWorkerId();
        initService = injector.getInstance(InitService.class);
        initService.publishRpcServices();
        heartBeatMessage.setPort(props.getInt("rpc.port"));
        heartBeatMessage.setTaskRunId(command.getTaskRunId());
        Thread heartbeatTask = new Thread(new HeartBeatTask(heartBeatMessage));
        heartbeatTask.start();
        Thread statusUpdateTask = new Thread(new StatusUpdateTask());
        statusUpdateTask.start();
        TaskAttemptMsg msg = launchOperator(command);
        int exitCode = msg.getTaskRunStatus().isSuccess() ? 0 : 1;
        try {
            logger.info("wait statusUpdate message send to executor");
            statusUpdateTask.join();
        } catch (InterruptedException e) {
            logger.info("wait statusUpdate message send to executor failed", e);
        }
        writeResult(out, msg);
        System.exit(exitCode);
    }


    //rpc
    public boolean killTask(Boolean abortByUser) {
        if (abortByUser) {
            abort = true;
        }
        cancel();
        return cancelled;
    }

    private synchronized void cancel() {
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

    //rpc
    public TaskAttemptMsg launchOperator(ExecCommand command) {
        TaskAttemptMsg msg = new TaskAttemptMsg();
        msg.setWorkerId(workerId);
        msg.setTaskRunId(command.getTaskRunId());
        msg.setTaskAttemptId(command.getTaskAttemptId());
        msg.setStartAt(DateTimeUtils.now());
        Thread thread = Thread.currentThread();
        ClassLoader cl = thread.getContextClassLoader();
        try {
            // 初始化
            initLogger(command.getLogPath());
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
            TaskAttemptMsg runningMsg = msg.copy();
            taskRunStatus = TaskRunStatus.RUNNING;
            runningMsg.setTaskRunStatus(TaskRunStatus.RUNNING);
            statusUpdate(runningMsg);

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

    private static HeartBeatMessage readConfig(String configFile) {
        try {
            return JsonCodec.MAPPER.readValue(new File(configFile), HeartBeatMessage.class);
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
        return new OperatorContextImpl(command.getConfig());
    }

    private void initLogger(String logPath) {
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

    private String trimPrefix(String logPath) {
        if (!logPath.startsWith("file:")) {
            throw new IllegalArgumentException("Not a file resource: " + logPath);
        }
        return logPath.substring(5);
    }

    private TaskAttemptMsg setCancelledMsg(TaskAttemptMsg msg) {
        if (!abort) {
            return setFailedMsg(msg);
        }
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

    class StatusUpdateTask implements Runnable {

        @Override
        public void run() {
            try {
                logger.debug("wait heartbeat start");
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                logger.error("try to sleep failed", e);
            }
            while (!cancelled) {
                try {
                    TaskAttemptMsg msg = statusUpdateQueue.take();
                    int sendTime = 0;
                    boolean updateSuccess = false;
                    while (!updateSuccess && sendTime <= RPC_RETRY_TIME) {
                        try {
                            if (sendTime > 0) {
                                Thread.sleep(HEARTBEAT_INTERVAL);
                            }
                            updateSuccess = executorFacade.statusUpdate(msg);
                            logger.info("send update task status message = {}, to executor result = {}", msg, updateSuccess);
                            if (!updateSuccess) {
                                sendTime++;
                            }
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

    class HeartBeatTask implements Runnable {

        private HeartBeatMessage heartBeatMessage;

        HeartBeatTask(HeartBeatMessage heartBeatMessage) {
            this.heartBeatMessage = heartBeatMessage;
        }

        @Override
        public void run() {
            while (!taskRunStatus.isFinished()) {
                try {
                    heartBeatMessage.setTaskRunStatus(taskRunStatus);
                    heartBeatMessage.setTimeoutTimes(0);
                    boolean result = executorFacade.heartBeat(heartBeatMessage);
                    Thread.sleep(HEARTBEAT_INTERVAL);
                    logger.debug("heart beat to executor result = {}", result);
                } catch (Exception e) {
                    logger.error("heart beat to executor time out", e);
                }
            }
        }
    }
}
