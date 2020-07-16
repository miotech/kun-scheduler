package com.miotech.kun.workflow.executor.mock;

import com.miotech.kun.workflow.core.execution.ConfigDef;
import com.miotech.kun.workflow.core.execution.KunOperator;
import com.miotech.kun.workflow.core.execution.logging.Logger;

public class TestOperator2 extends KunOperator {
    private Logger logger;

    public void init() {
        this.logger = this.getContext().getLogger();
    }

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
}
