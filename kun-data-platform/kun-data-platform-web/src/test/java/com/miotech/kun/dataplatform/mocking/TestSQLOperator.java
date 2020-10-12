package com.miotech.kun.dataplatform.mocking;

import com.miotech.kun.workflow.core.execution.ConfigDef;
import com.miotech.kun.workflow.core.execution.KunOperator;
import com.miotech.kun.workflow.core.execution.NopResolver;
import com.miotech.kun.workflow.core.execution.Resolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class TestSQLOperator extends KunOperator {
    private static final Logger logger = LoggerFactory.getLogger(TestSQLOperator.class);

    @Override
    public boolean run() {
        String sql = getContext().getConfig().getString("sparkSQL");
        logger.info("Test run sql: {}", sql);
        return true;
    }

    @Override
    public void abort() {

    }

    @Override
    public ConfigDef config() {
        return new ConfigDef()
                .define("sparkSQL", ConfigDef.Type.STRING, true, "sql script", "sql");
    }

    @Override
    public Resolver getResolver() {
        return new NopResolver();
    }
}
