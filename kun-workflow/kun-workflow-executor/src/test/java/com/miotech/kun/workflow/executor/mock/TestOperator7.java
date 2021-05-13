package com.miotech.kun.workflow.executor.mock;

import com.google.common.util.concurrent.Uninterruptibles;
import com.miotech.kun.workflow.core.execution.ConfigDef;
import com.miotech.kun.workflow.core.execution.KunOperator;
import com.miotech.kun.workflow.core.execution.NopResolver;
import com.miotech.kun.workflow.core.execution.Resolver;
import com.miotech.kun.workflow.utils.DateTimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.concurrent.TimeUnit;

public class TestOperator7 extends KunOperator {

    private static final Logger logger = LoggerFactory.getLogger(TestOperator7.class);

    private volatile boolean aborted = false;

    @SuppressWarnings("java:S2925")
    public boolean run() {
        logger.info("START RUNNING");
        OffsetDateTime start = DateTimeUtils.now();
        while (true) {
            OffsetDateTime now = DateTimeUtils.now();
            Duration duration = Duration.between(start, now);
            if (aborted || duration.getSeconds() > 10L) {
                logger.info("TestOperator7 is aborting...");
                Uninterruptibles.sleepUninterruptibly(5000, TimeUnit.MILLISECONDS);
                break;
            } else {
                logger.info("TestOperator7 is not aborted yet");
                Uninterruptibles.sleepUninterruptibly(500, TimeUnit.MILLISECONDS);
            }
        }
        return true;
    }

    @Override
    public void abort() {
        aborted = true;
    }

    @Override
    public ConfigDef config() {
        return new ConfigDef();
    }

    @Override
    public Resolver getResolver() {
        return new NopResolver();
    }
}
