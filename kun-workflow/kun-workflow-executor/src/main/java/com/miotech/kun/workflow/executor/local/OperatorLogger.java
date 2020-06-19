package com.miotech.kun.workflow.executor.local;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.OutputStreamAppender;
import com.miotech.kun.commons.utils.ExceptionUtils;
import com.miotech.kun.workflow.core.execution.logging.Logger;
import com.miotech.kun.workflow.core.resource.Resource;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class OperatorLogger implements Logger {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(OperatorLogger.class);

    private static final Level LOG_LEVEL = Level.DEBUG;

    private final long attemptId;
    private final Resource resource;
    private final org.slf4j.Logger delegate;

    public OperatorLogger(long attemptId, Resource resource) {
        this.attemptId = attemptId;
        this.resource = resource;

        try {
            this.delegate = buildLogger();
        } catch (Exception e) {
            logger.error("Failed to build logger.", e);
            throw ExceptionUtils.wrapIfChecked(e);
        }
    }

    @Override
    public void debug(String format, Object... arguments) {
        delegate.debug(format, arguments);
    }

    @Override
    public void info(String format, Object... arguments) {
        delegate.info(format, arguments);
    }

    @Override
    public void warn(String format, Object... arguments) {
        delegate.warn(format, arguments);
    }

    @Override
    public void error(String format, Object... arguments) {
        delegate.error(format, arguments);
    }

    private org.slf4j.Logger buildLogger() throws IOException {
        ch.qos.logback.classic.Logger lg = (ch.qos.logback.classic.Logger)
                LoggerFactory.getLogger("kun-executor-task-" + attemptId + "-logger");
        lg.setLevel(LOG_LEVEL);
        lg.setAdditive(false);

        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(lg.getLoggerContext());
        encoder.setPattern("%d{HH:mm:ss.SSS} %-5level - %msg%n");
        encoder.start();

        OutputStreamAppender<ILoggingEvent> appender = null;
        appender = new OutputStreamAppender<>();
        appender.setContext(lg.getLoggerContext());
        appender.setOutputStream(resource.getOutputStream());
        appender.setEncoder(encoder);
        appender.start();

        lg.addAppender(appender);

        return lg;
    }
}
