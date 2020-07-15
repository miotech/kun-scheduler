package com.miotech.kun.workflow.executor.local;

import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.sift.MDCBasedDiscriminator;
import ch.qos.logback.classic.sift.SiftingAppender;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.OutputStreamAppender;
import ch.qos.logback.core.helpers.NOPAppender;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.sift.AppenderFactory;
import com.miotech.kun.commons.utils.ExceptionUtils;
import com.miotech.kun.workflow.common.resource.ResourceLoader;
import com.miotech.kun.workflow.common.taskrun.service.TaskRunService;
import com.miotech.kun.workflow.core.resource.Resource;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.io.OutputStream;

@Singleton
public class TaskAttemptSiftingAppender extends SiftingAppender {
    public static final String SIFTING_KEY = "taskAttemptId";
    private static final String NULL = "none";

    private static final Appender<ILoggingEvent> NOPAppender;

    static {
        NOPAppender = new NOPAppender<>();
        NOPAppender.start();
    }

    private final ResourceLoader resourceLoader;

    private final TaskRunService taskRunService;

    @Inject
    public TaskAttemptSiftingAppender(ResourceLoader resourceLoader, TaskRunService taskRunService) {
        this.resourceLoader = resourceLoader;
        this.taskRunService = taskRunService;
        init();
    }

    private void init() {
        MDCBasedDiscriminator discriminator = new MDCBasedDiscriminator();
        discriminator.setKey(SIFTING_KEY);
        discriminator.setDefaultValue(NULL);
        discriminator.start();
        setDiscriminator(discriminator);
        setAppenderFactory(new TaskAttemptAppenderFactory());
    }

    private String newLogPath(Long attemptId) {
        return taskRunService.logPathOfTaskAttempt(attemptId);
    }

    private class TaskAttemptAppenderFactory implements AppenderFactory<ILoggingEvent> {
        @Override
        public Appender<ILoggingEvent> buildAppender(Context context, String discriminatingValue) throws JoranException {
            if (discriminatingValue.equals(NULL)) {
                return buildNOPAppender();
            }

            String logPath = newLogPath(Long.parseLong(discriminatingValue));
            Resource logResource = resourceLoader.getResource(logPath, true);
            OutputStream out;
            try {
                out = logResource.getOutputStream();
            } catch (IOException e) {
                throw ExceptionUtils.wrapIfChecked(e);
            }

            PatternLayoutEncoder encoder = new PatternLayoutEncoder();
            encoder.setContext(context);
            encoder.setPattern("%d{HH:mm:ss.SSS} %-5level - %msg%n");
            encoder.start();

            OutputStreamAppender<ILoggingEvent> appender;
            appender = new OutputStreamAppender<>();
            appender.setContext(context);
            appender.setOutputStream(out);
            appender.setEncoder(encoder);
            appender.start();

            return appender;
        }

        private NOPAppender<ILoggingEvent> buildNOPAppender() {
            NOPAppender<ILoggingEvent> nopa = new NOPAppender<>();
            nopa.setContext(context);
            nopa.start();
            return nopa;
        }
    }
}
