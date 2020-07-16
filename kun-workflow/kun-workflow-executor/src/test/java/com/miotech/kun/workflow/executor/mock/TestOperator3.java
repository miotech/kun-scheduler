package com.miotech.kun.workflow.executor.mock;

import com.miotech.kun.workflow.core.execution.ConfigDef;
import com.miotech.kun.workflow.core.execution.KunOperator;

public class TestOperator3 extends KunOperator {
    public boolean run() {
        throw new IllegalStateException("Failure");
    }

    @Override
    public ConfigDef config() {
        return new ConfigDef();
    }

    @Override
    public void abort() {

    }
}
