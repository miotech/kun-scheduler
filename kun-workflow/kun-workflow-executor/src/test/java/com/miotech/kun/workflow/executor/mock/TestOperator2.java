package com.miotech.kun.workflow.executor.mock;

import com.miotech.kun.workflow.core.execution.ConfigDef;
import com.miotech.kun.workflow.core.execution.KunOperator;
import com.miotech.kun.workflow.core.execution.NopResolver;
import com.miotech.kun.workflow.core.execution.Resolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestOperator2 extends KunOperator {
    private static final Logger logger = LoggerFactory.getLogger(TestOperator2.class);

    public boolean run() {
        this.logger.info("Execution Failed");
        return false;
    }

    @Override
    public ConfigDef config() {
        return new ConfigDef();
    }

    @Override
    public void abort() {

    }

    @Override
    public Resolver getResolver() {
        return new NopResolver();
    }
}
