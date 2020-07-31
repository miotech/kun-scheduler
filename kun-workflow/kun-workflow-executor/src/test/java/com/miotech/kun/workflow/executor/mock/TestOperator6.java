package com.miotech.kun.workflow.executor.mock;

import com.google.common.util.concurrent.Uninterruptibles;
import com.miotech.kun.workflow.core.execution.ConfigDef;
import com.miotech.kun.workflow.core.execution.KunOperator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * An operator simulating a task with 5 seconds execution duration
 */
public class TestOperator6 extends KunOperator {
    private static final Logger logger = LoggerFactory.getLogger(TestOperator6.class);

    private volatile boolean aborted = false;

    @SuppressWarnings("java:S2925")
    public boolean run() {
        logger.info("START RUNNING");
        Uninterruptibles.sleepUninterruptibly(30000, TimeUnit.MILLISECONDS);
        return true;
    }

    @Override
    public void abort() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ConfigDef config() {
        return new ConfigDef();
    }
}
