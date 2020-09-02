package com.miotech.kun.workflow.executor.mock;

import com.miotech.kun.workflow.core.execution.ConfigDef;
import com.miotech.kun.workflow.core.execution.KunOperator;
import com.miotech.kun.workflow.core.execution.Resolver;

public class TestOperator3 extends KunOperator {
    public boolean run() {
        throw new IllegalStateException("Failure");
    }

    @Override
    public ConfigDef config() {
        return new ConfigDef();
    }

    @Override
    public Resolver getResolver() {
        return new TestOperatorResolver();
    }

    @Override
    public void abort() {

    }
}
