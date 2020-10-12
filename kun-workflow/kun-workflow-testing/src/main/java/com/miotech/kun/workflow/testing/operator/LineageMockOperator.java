package com.miotech.kun.workflow.testing.operator;

import com.miotech.kun.workflow.core.execution.ConfigDef;
import com.miotech.kun.workflow.core.execution.KunOperator;
import com.miotech.kun.workflow.core.execution.Resolver;

public class LineageMockOperator extends KunOperator {
    @Override
    public boolean run() {
        return true;
    }

    @Override
    public void abort() {
        // no operations
    }

    @Override
    public ConfigDef config() {
        return new ConfigDef();
    }

    @Override
    public Resolver getResolver() {
        return new LineageMockOperatorResolver();
    }
}
