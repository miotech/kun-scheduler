package com.miotech.kun.workflow.client.mock;

import com.miotech.kun.workflow.core.execution.ConfigDef;
import com.miotech.kun.workflow.core.execution.KunOperator;

public class NopOperator extends KunOperator {
    @Override
    public boolean run() {
        return true;
    }

    @Override
    public void abort() {

    }

    @Override
    public ConfigDef config() {
        return new ConfigDef();
    }
}
