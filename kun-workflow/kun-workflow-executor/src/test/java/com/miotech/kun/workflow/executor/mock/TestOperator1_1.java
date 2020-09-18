package com.miotech.kun.workflow.executor.mock;

import com.miotech.kun.workflow.core.execution.ConfigDef;
import com.miotech.kun.workflow.core.execution.KunOperator;
import com.miotech.kun.workflow.core.execution.NopResolver;
import com.miotech.kun.workflow.core.execution.Resolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestOperator1_1 extends KunOperator {
    private static final Logger logger = LoggerFactory.getLogger(TestOperator1_1.class);

    public boolean run() {
        final String name = "world2";
        logger.info("Hello, {}!", new Object[] { name });
        return true;
    }

    @Override
    public void abort() {

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
