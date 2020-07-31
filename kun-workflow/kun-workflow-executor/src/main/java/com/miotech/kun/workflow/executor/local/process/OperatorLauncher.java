package com.miotech.kun.workflow.executor.local.process;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.FileAppender;
import com.miotech.kun.commons.utils.ExceptionUtils;
import com.miotech.kun.workflow.core.execution.KunOperator;
import com.miotech.kun.workflow.core.execution.OperatorContext;
import com.miotech.kun.workflow.core.execution.TaskAttemptReport;
import com.miotech.kun.workflow.executor.ExecCommand;
import com.miotech.kun.workflow.executor.ExecResult;
import com.miotech.kun.workflow.executor.JsonCodec;
import com.miotech.kun.workflow.executor.local.OperatorContextImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;

public class OperatorLauncher {
    private static final Logger logger = LoggerFactory.getLogger(OperatorLauncher.class);

    private volatile boolean cancelled;
    private volatile KunOperator operator;

    public void launch(String[] args) {
        String in = args[0];
        String out = args[1];

        // shutdown hook
        Thread t = new Thread(() -> {
            while (true) {
                String sig = readSignal();
                logger.debug("Received signal: {}.", sig);
                if (sig.equals(ProcessTaskRunner.SIG_TERM)) {
                    cancel();
                }
            }
        });
        t.setDaemon(true);
        t.setName("operator-signal-receiver");
        t.start();

        ExecCommand command = readCommand(in);
        ExecResult result = execute(command);
        writeResult(out, result);

        System.exit(exitCode(result));
    }

    private ExecResult execute(ExecCommand command) {
        try {
            // 初始化
            initLogger(command.getLogPath());

            // 加载Operator
            operator = loadOperator(command.getJarPath(), command.getClassName());
            operator.setContext(initContext(command));

            if (cancelled) {
                logger.info("Operator is cancelled, abort execution.");
                return cancelledResult();
            }

            // 初始化Operator
            logger.info("Initializing operator...");
            operator.init();
            logger.info("Operator has initialized successfully.");

            if (cancelled) {
                logger.info("Operator is cancelled, abort execution.");
                return cancelledResult();
            }

            // 运行
            logger.info("Operator start running.");
            boolean success = operator.run();
            logger.info("Operator execution finished. success={}", success);

            if (cancelled) {
                return cancelledResult();
            } else if (success) {
                return successfulResult(operator.getReport().orElse(TaskAttemptReport.BLANK));
            } else {
                return failedResult();
            }
        } catch (Throwable e) {
            logger.error("Unexpected exception occurred.", e);
            return failedResult();
        }
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

    private String readSignal() {
        try {
            byte[] buf = new byte[1024];
            int n = System.in.read(buf);
            return n != -1 ? new String(buf, 0, n) : "";
        } catch (IOException e) {
            logger.error("Failed to read signal", e);
            throw ExceptionUtils.wrapIfChecked(e);
        }
    }

    private ExecCommand readCommand(String inputFile) {
        try {
            return JsonCodec.MAPPER.readValue(new File(inputFile), ExecCommand.class);
        } catch (IOException e) {
            logger.error("Failed to read input. path={}", inputFile, e);
            throw new IllegalStateException(e);
        }
    }

    private void writeResult(String outputFile, ExecResult result) {
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

    private ExecResult successfulResult(TaskAttemptReport report) {
        ExecResult result = new ExecResult();
        result.setSuccess(true);
        result.setCancelled(false);
        result.setReport(report);
        return result;
    }

    private ExecResult failedResult() {
        ExecResult result = new ExecResult();
        result.setSuccess(false);
        result.setCancelled(false);
        result.setReport(TaskAttemptReport.BLANK);
        return result;
    }

    private ExecResult cancelledResult() {
        ExecResult result = new ExecResult();
        result.setSuccess(false);
        result.setCancelled(true);
        result.setReport(TaskAttemptReport.BLANK);
        return result;
    }

    private String trimPrefix(String logPath) {
        if (!logPath.startsWith("file:")) {
            throw new IllegalArgumentException("Not a file resource: " + logPath);
        }
        return logPath.substring(5);
    }

    private int exitCode(ExecResult result) {
        return result.isSuccess() ? 0 : 1;
    }

    /* ---------------------------------------- */
    /* ------------ main method --------------- */
    /* ---------------------------------------- */

    public static void main(String[] args) {
        new OperatorLauncher().launch(args);
    }
}
