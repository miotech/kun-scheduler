package com.miotech.kun.workflow.scheduler;

import com.miotech.kun.workflow.core.execution.ConfigDef;
import com.miotech.kun.workflow.core.execution.KunOperator;
import com.miotech.kun.workflow.core.execution.Resolver;

public class TestOperator1 extends KunOperator {
    @Override
    public boolean run() {
        return false;
    }

    @Override
    public ConfigDef config() {
        ConfigDef configDef = new ConfigDef();
        configDef.define("var1", ConfigDef.Type.STRING, "default1", true, "", "");
        configDef.define("var2", ConfigDef.Type.STRING, "default2", true, "", "");
        return configDef;
    }

    @Override
    public Resolver getResolver() {
        // TODO: implement this
        return null;
    }

    @Override
    public void abort() {

    }
}
