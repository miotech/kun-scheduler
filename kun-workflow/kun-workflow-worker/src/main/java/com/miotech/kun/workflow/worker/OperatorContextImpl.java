package com.miotech.kun.workflow.worker;

import com.miotech.kun.workflow.core.execution.Config;
import com.miotech.kun.workflow.core.execution.OperatorContext;
import com.miotech.kun.workflow.core.resource.Resource;

public class OperatorContextImpl implements OperatorContext {
    private final Config config;

    public OperatorContextImpl(Config config) {
        this.config = config;
    }

    @Override
    public Config getConfig() {
        return config;
    }

    @Override
    public Resource getResource(String path) {
        throw new UnsupportedOperationException("Not implemented yet.");
    }
}

