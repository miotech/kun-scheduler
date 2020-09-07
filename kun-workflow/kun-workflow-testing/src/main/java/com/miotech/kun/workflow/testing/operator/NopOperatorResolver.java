package com.miotech.kun.workflow.testing.operator;

import com.miotech.kun.metadata.core.model.DataStore;
import com.miotech.kun.workflow.core.execution.Config;
import com.miotech.kun.workflow.core.execution.Resolver;

import java.util.ArrayList;
import java.util.List;

public class NopOperatorResolver implements Resolver {
    private List<DataStore> upstreamDataStores;
    private List<DataStore> downstreamDataStores;

    public NopOperatorResolver() {
        this.upstreamDataStores = new ArrayList<>();
        this.downstreamDataStores = new ArrayList<>();
    }

    public NopOperatorResolver(List<DataStore> upstreamDataStores, List<DataStore> downstreamDataStores) {
        this.upstreamDataStores = upstreamDataStores;
        this.downstreamDataStores = downstreamDataStores;
    }

    @Override
    public List<DataStore> resolveUpstreamDataStore(Config config) {
        return this.upstreamDataStores;
    }

    @Override
    public List<DataStore> resolveDownstreamDataStore(Config config) {
        return this.downstreamDataStores;
    }
}
