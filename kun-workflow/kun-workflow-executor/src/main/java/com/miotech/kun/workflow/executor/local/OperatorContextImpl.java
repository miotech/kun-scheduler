package com.miotech.kun.workflow.executor.local;

import com.miotech.kun.workflow.core.execution.Config;
import com.miotech.kun.workflow.core.execution.OperatorContext;
import com.miotech.kun.workflow.core.execution.logging.Logger;
import com.miotech.kun.workflow.core.model.taskrun.TaskAttempt;
import com.miotech.kun.workflow.core.resource.Resource;

public class OperatorContextImpl implements OperatorContext {
    private final Config config;
    private final Logger logger;

    public OperatorContextImpl(TaskAttempt attempt, Resource logResource) {
        this.config = attempt.getTaskRun().getConfig();
        this.logger = buildLogger(attempt, logResource);
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    @Override
    public Config getConfig() {
        return config;
    }

    @Override
    public Resource getResource(String path) {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    private Logger buildLogger(TaskAttempt attempt, Resource logResource) {
        return new OperatorLogger(attempt.getId(), logResource);
    }
}
