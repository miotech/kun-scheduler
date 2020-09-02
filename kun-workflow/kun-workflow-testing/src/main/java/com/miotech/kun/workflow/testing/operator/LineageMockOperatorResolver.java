package com.miotech.kun.workflow.testing.operator;

import com.miotech.kun.workflow.core.execution.Config;
import com.miotech.kun.workflow.core.execution.ConfigDef;
import com.miotech.kun.workflow.core.execution.KunOperator;
import com.miotech.kun.workflow.core.execution.Resolver;
import com.miotech.kun.workflow.core.model.lineage.DataStore;
import com.miotech.kun.workflow.testing.factory.MockDataStoreFactory;

import java.util.ArrayList;
import java.util.List;

public class LineageMockOperatorResolver implements Resolver {

    @Override
    public List<DataStore> resolveUpstreamDataStore(Config config) {
        Integer upstreamStoreCount = config.getInt("upstreamStoreCount", 1);
        List<DataStore> upstreamDataStores = new ArrayList<>();
        for (int i = 0; i < upstreamStoreCount; i += 1) {
            upstreamDataStores.add(MockDataStoreFactory.getMockDataStore());
        }
        return upstreamDataStores;
    }

    @Override
    public List<DataStore> resolveDownstreamDataStore(Config config) {
        Integer downstreamStoreCount = config.getInt("downstreamStoreCount", 1);
        List<DataStore> downstreamDataStores = new ArrayList<>();
        for (int i = 0; i < downstreamStoreCount; i += 1) {
            downstreamDataStores.add(MockDataStoreFactory.getMockDataStore());
        }
        return downstreamDataStores;
    }
}
