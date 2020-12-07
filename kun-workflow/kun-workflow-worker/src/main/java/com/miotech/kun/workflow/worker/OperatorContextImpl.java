package com.miotech.kun.workflow.worker;

import com.miotech.kun.workflow.core.execution.Config;
import com.miotech.kun.workflow.core.execution.OperatorContext;
import com.miotech.kun.workflow.core.resource.Resource;

public class OperatorContextImpl implements OperatorContext {
    private final Config config;
    private final Long taskRunId;

    public OperatorContextImpl(Config config,Long taskRunId) {
        this.config = config;
        this.taskRunId = taskRunId;
    }

    @Override
    public Config getConfig() {
        return config;
    }

    @Override
    public Long getTaskRunId() {
        return taskRunId;
    }

    @Override
    public Resource getResource(String path) {
        throw new UnsupportedOperationException("Not implemented yet.");
    }
}

