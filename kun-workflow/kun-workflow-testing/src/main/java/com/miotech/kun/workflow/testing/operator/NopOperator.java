package com.miotech.kun.workflow.testing.operator;

import com.miotech.kun.workflow.core.execution.ConfigDef;
import com.miotech.kun.workflow.core.execution.KunOperator;

public class NopOperator extends KunOperator {
    @Override
    public boolean run() {
        return true;
    }

    @Override
    public void abort() {
        // nop
    }

    @Override
    public ConfigDef config() {
        return new ConfigDef()
                .define("testKey1", ConfigDef.Type.BOOLEAN, true, "test key 1", "testKey1");
    }
}
