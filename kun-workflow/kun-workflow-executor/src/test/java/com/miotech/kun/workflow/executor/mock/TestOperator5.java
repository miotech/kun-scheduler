package com.miotech.kun.workflow.executor.mock;

import com.google.common.util.concurrent.Uninterruptibles;
import com.miotech.kun.workflow.core.execution.ConfigDef;
import com.miotech.kun.workflow.core.execution.KunOperator;
import com.miotech.kun.workflow.core.execution.NopResolver;
import com.miotech.kun.workflow.core.execution.Resolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * An operator simulating a task with 60 seconds execution duration
 * When executing timeout, it will be trapped into infinite loop
 */
public class TestOperator5 extends KunOperator {
    private static final Logger logger = LoggerFactory.getLogger(TestOperator5.class);

    private boolean isAbort = false;

    @SuppressWarnings("java:S2925")
    public boolean run() {
        logger.info("START RUNNING");
        Uninterruptibles.sleepUninterruptibly(120000, TimeUnit.MILLISECONDS);
        return true;
    }

    @Override
    public void abort() {
        isAbort = true;
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
