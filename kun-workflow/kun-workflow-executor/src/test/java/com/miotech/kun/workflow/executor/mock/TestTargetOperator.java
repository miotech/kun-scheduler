package com.miotech.kun.workflow.executor.mock;

import com.miotech.kun.workflow.core.execution.ConfigDef;
import com.miotech.kun.workflow.core.execution.KunOperator;
import com.miotech.kun.workflow.core.execution.NopResolver;
import com.miotech.kun.workflow.core.execution.Resolver;
import com.miotech.kun.workflow.core.model.executetarget.ExecuteTarget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestTargetOperator extends KunOperator {

    private static final Logger logger = LoggerFactory.getLogger(TestTargetOperator.class);
    private volatile boolean aborted = false;
    private final String expectTargetName = "test";

    @SuppressWarnings("java:S2925")
    public boolean run() {
        ExecuteTarget target = getContext().getExecuteTarget();
        String targetName = target.getName();
        logger.info("running target is " + targetName);
        if (targetName == null || !targetName.equals(expectTargetName)) {
            return false;
        }
        return true;

    }

    @Override
    public void abort() {
        aborted = true;
    }

    @Override
    public ConfigDef config() {
        return new ConfigDef();
    }

    @Override
    public Resolver getResolver() {
        return new NopResolver();
    }
}
