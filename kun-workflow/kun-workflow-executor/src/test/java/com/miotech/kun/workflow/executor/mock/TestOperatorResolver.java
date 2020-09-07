package com.miotech.kun.workflow.executor.mock;

import com.miotech.kun.metadata.core.model.DataStore;
import com.miotech.kun.workflow.core.execution.Config;
import com.miotech.kun.workflow.core.execution.Resolver;

import java.util.LinkedList;
import java.util.List;

public class TestOperatorResolver implements Resolver {
    @Override
    public List<DataStore> resolveUpstreamDataStore(Config config) {
        return new LinkedList<>();
    }

    @Override
    public List<DataStore> resolveDownstreamDataStore(Config config) {
        return new LinkedList<>();
    }
}
