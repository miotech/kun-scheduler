package com.miotech.kun.workflow.testing.executor;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.OutputStreamAppender;
import com.google.common.collect.ImmutableMap;
import com.miotech.kun.commons.utils.ExceptionUtils;
import com.miotech.kun.workflow.core.execution.Config;
import com.miotech.kun.workflow.core.execution.KunOperator;
import com.miotech.kun.workflow.core.execution.OperatorContext;
import com.miotech.kun.workflow.core.execution.logging.Logger;
import com.miotech.kun.workflow.core.model.lineage.DataStore;
import com.miotech.kun.workflow.core.resource.Resource;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MockOperatorContextImpl implements OperatorContext {

    private Map<String, Object> configMap = new HashMap();

    private List<DataStore> inlets = Collections.emptyList();
    private List<DataStore> outlets = Collections.emptyList();

    private Resource resource;
    private KunOperator operator;

    public void setParam(String key, String value) {
        this.configMap.put(key, value);
    }

    public void setParams(Map<String, String> params) {
        this.configMap.putAll(params);
    }

    public MockOperatorContextImpl(Resource resource, KunOperator operator) {
        this.resource = resource;
        this.operator = operator;
    }

    @Override
    public Resource getResource(String path) {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    @Override
    public Config getConfig() {
        return new Config(configMap)
                .overrideBy(new Config(operator.config(), ImmutableMap.of()));
    }

    @Override
    public Logger getLogger() {
        return new TestOperatorLogger(resource);
    }

    public List<DataStore> getInlets() {
        return inlets;
    }

    public List<DataStore> getOutlets() {
        return outlets;
    }

    private class TestOperatorLogger implements Logger {
        private final org.slf4j.Logger logger = LoggerFactory.getLogger(TestOperatorLogger.class);

        private final Level LOG_LEVEL = Level.DEBUG;

        private final Resource resource;
        private final org.slf4j.Logger delegate;

        public TestOperatorLogger(Resource resource) {
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
                    LoggerFactory.getLogger("kun-executor-test-logger");
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

}
