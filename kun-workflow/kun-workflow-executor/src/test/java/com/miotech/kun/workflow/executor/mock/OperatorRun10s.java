package com.miotech.kun.workflow.executor.mock;

import com.google.common.util.concurrent.Uninterruptibles;
import com.miotech.kun.workflow.core.execution.ConfigDef;
import com.miotech.kun.workflow.core.execution.KunOperator;
import com.miotech.kun.workflow.core.execution.NopResolver;
import com.miotech.kun.workflow.core.execution.Resolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class OperatorRun10s extends KunOperator {
    private static final Logger logger = LoggerFactory.getLogger(OperatorRun10s.class);

    @SuppressWarnings("java:S2925")
    public boolean run() {
        logger.info("START RUNNING");
        Uninterruptibles.sleepUninterruptibly(10000, TimeUnit.MILLISECONDS);
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

    @Override
    public Resolver getResolver() {
        return new NopResolver();
    }
}