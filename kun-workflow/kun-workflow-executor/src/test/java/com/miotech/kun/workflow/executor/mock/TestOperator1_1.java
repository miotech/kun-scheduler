package com.miotech.kun.workflow.executor.mock;

import com.miotech.kun.workflow.core.execution.Operator;
import com.miotech.kun.workflow.core.execution.logging.Logger;

public class TestOperator1_1 extends Operator {
    private Logger logger;

    public void init() {
        this.logger = this.getContext().getLogger();
    }

    public boolean run() {
        final String name = "world2";
        logger.info("Hello, {}!", new Object[] { name });
        return true;
    }
}
