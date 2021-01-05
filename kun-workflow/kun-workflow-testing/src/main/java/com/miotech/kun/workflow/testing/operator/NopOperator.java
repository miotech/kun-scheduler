package com.miotech.kun.workflow.testing.operator;

import com.miotech.kun.workflow.core.execution.ConfigDef;
import com.miotech.kun.workflow.core.execution.KunOperator;
import com.miotech.kun.workflow.core.execution.NopResolver;
import com.miotech.kun.workflow.core.execution.Resolver;

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
                .define("testKey1", ConfigDef.Type.BOOLEAN,false, true, "test key 1", "testKey1");
    }

    @Override
    public Resolver getResolver() {
        return new NopResolver();
    }
}
