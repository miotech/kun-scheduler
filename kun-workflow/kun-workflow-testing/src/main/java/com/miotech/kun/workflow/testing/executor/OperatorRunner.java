package com.miotech.kun.workflow.testing.executor;

import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.OutputStreamAppender;
import com.miotech.kun.commons.utils.ExceptionUtils;
import com.miotech.kun.workflow.common.resource.ResourceLoader;
import com.miotech.kun.workflow.common.resource.ResourceLoaderImpl;
import com.miotech.kun.workflow.core.execution.KunOperator;
import com.miotech.kun.workflow.core.execution.OperatorContext;
import com.miotech.kun.workflow.core.execution.TaskAttemptReport;
import com.miotech.kun.workflow.core.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class OperatorRunner {
    private static final Logger logger = LoggerFactory.getLogger(OperatorRunner.class);

    private final MockOperatorContextImpl context;
    private final KunOperator operator;
    private final Resource resource;

    public OperatorRunner(KunOperator operator) {
       this.resource = prepareResource();
       this.operator = operator;
       this.context = new MockOperatorContextImpl(operator);
       operator.setContext(context);
    }

    public void setConfigKey(String key, String value) {
        this.context.setParam(key, value);
    }

    public void setConfig(Map<String, String> params) {
        this.context.setParams(params);
    }

    private Resource prepareResource() {
        Path tempFile = null;
        try {
            tempFile = Files.createTempFile("tempfiles", ".log");
        } catch (IOException e) {
            throw ExceptionUtils.wrapIfChecked(e);
        }
        ResourceLoader resourceLoader = new ResourceLoaderImpl();
        return resourceLoader.getResource(tempFile.toString());
    }

    public boolean run() {
        try {
            appendLogger(this.resource);
        } catch (IOException e) {
            throw ExceptionUtils.wrapIfChecked(e);
        }
        logger.info("Init operator ");
        this.operator.init();
        logger.info("Run operator ");
        return this.operator.run();
    }

    public void abortAfter(int seconds, Consumer<OperatorContext> function) {
        new Thread(() -> {
            try {
                Thread.sleep(seconds * 1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            logger.info("Prepare abort context ");
            if (function != null) function.accept(context);
            logger.info("Stop operator ");
            this.operator.abort();
        }).start();

    }

    public Optional<TaskAttemptReport> getReport() {
        return this.operator.getReport();
    }

    public List<String> getLog() {
        try(InputStream inputStream = resource.getInputStream()) {
            return new BufferedReader(new InputStreamReader(inputStream,
                            StandardCharsets.UTF_8)).lines()
                            .collect(Collectors.toList());
        } catch (IOException e) {
            throw ExceptionUtils.wrapIfChecked(e);
        }
    }

    public void appendLogger(Resource resource) throws IOException {
        ch.qos.logback.classic.Logger rootLogger
                = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);

        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(rootLogger.getLoggerContext());
        encoder.setPattern("%d{HH:mm:ss.SSS} %-5level - %msg%n");
        encoder.start();

        OutputStreamAppender<ILoggingEvent> appender = new OutputStreamAppender<>();
        appender.setOutputStream(resource.getOutputStream());
        appender.setImmediateFlush(true);
        appender.setEncoder(encoder);
        appender.setContext(rootLogger.getLoggerContext());
        appender.start();
        rootLogger.addAppender(appender);
    }
}
