package com.miotech.kun.workflow.common.operator.service;

import com.miotech.kun.metadata.core.model.DataStore;
import com.miotech.kun.metadata.core.model.DataStoreType;
import com.miotech.kun.workflow.core.execution.Config;
import com.miotech.kun.workflow.core.execution.ConfigDef;
import com.miotech.kun.workflow.core.execution.KunOperator;
import com.miotech.kun.workflow.core.execution.Resolver;
import com.miotech.kun.workflow.testing.factory.MockDataStoreFactory;

import java.util.ArrayList;
import java.util.List;

public class LineageMockOperator extends KunOperator {
    @Override
    public boolean run() {
        return true;
    }

    @Override
    public void abort() {
        // no operations
    }

    @Override
    public ConfigDef config() {
        return new ConfigDef()
                .define("upstreamStoreCount", ConfigDef.Type.INT, true, "test key 1", "testKey1")
                .define("downstreamStoreCount", ConfigDef.Type.INT, true, "test key 2", "testKey2");
    }

    @Override
    public Resolver getResolver() {
        return new LineageMockOperatorResolver();
    }

    public static class LineageMockOperatorResolver implements Resolver {
        @Override
        public List<DataStore> resolveUpstreamDataStore(Config config) {
            Integer upstreamStoreCount = config.getInt("upstreamStoreCount", 1);
            List<DataStore> upstreamDataStores = new ArrayList<>();
            for (int i = 0; i < upstreamStoreCount; i += 1) {
                upstreamDataStores.add(MockDataStoreFactory.getMockDataStore(DataStoreType.MYSQL_TABLE));
            }
            return upstreamDataStores;
        }

        @Override
        public List<DataStore> resolveDownstreamDataStore(Config config) {
            Integer downstreamStoreCount = config.getInt("downstreamStoreCount", 1);
            List<DataStore> downstreamDataStores = new ArrayList<>();
            for (int i = 0; i < downstreamStoreCount; i += 1) {
                downstreamDataStores.add(MockDataStoreFactory.getMockDataStore(DataStoreType.MYSQL_TABLE));
            }
            return downstreamDataStores;
        }
    }
}
