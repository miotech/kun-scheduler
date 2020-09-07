package com.miotech.kun.workflow.testing.operator;

import com.miotech.kun.metadata.core.model.DataStore;
import com.miotech.kun.workflow.core.execution.Config;
import com.miotech.kun.workflow.core.execution.ConfigDef;
import com.miotech.kun.workflow.core.execution.KunOperator;
import com.miotech.kun.workflow.core.execution.Resolver;

import java.util.ArrayList;
import java.util.List;

public class NopOperator extends KunOperator {
    @Override
    public boolean run() {
        return true;
    }

    @Override
    public void abort() {
        // nop
    }

    @Override
    public ConfigDef config() {
        return new ConfigDef()
                .define("testKey1", ConfigDef.Type.BOOLEAN, true, "test key 1", "testKey1");
    }

    @Override
    public Resolver getResolver() {
        return new NopOperatorResolver();
    }

    public static class NopOperatorResolver implements Resolver {
        @Override
        public List<DataStore> resolveUpstreamDataStore(Config config) {
            return new ArrayList<>();
        }

        @Override
        public List<DataStore> resolveDownstreamDataStore(Config config) {
            return new ArrayList<>();
        }
    }
}
