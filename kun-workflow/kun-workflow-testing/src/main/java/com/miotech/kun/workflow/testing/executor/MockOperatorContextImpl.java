package com.miotech.kun.workflow.testing.executor;

import com.google.common.collect.ImmutableMap;
import com.miotech.kun.metadata.core.model.dataset.DataStore;
import com.miotech.kun.workflow.core.execution.Config;
import com.miotech.kun.workflow.core.execution.KunOperator;
import com.miotech.kun.workflow.core.execution.OperatorContext;
import com.miotech.kun.workflow.core.resource.Resource;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MockOperatorContextImpl implements OperatorContext {

    private Map<String, Object> configMap = new HashMap();

    private List<DataStore> inlets = Collections.emptyList();
    private List<DataStore> outlets = Collections.emptyList();
    private Long taskRunId = 1l;

    private KunOperator operator;

    public void setParam(String key, String value) {
        this.configMap.put(key, value);
    }

    public void setParams(Map<String, String> params) {
        this.configMap.putAll(params);
    }

    public MockOperatorContextImpl(KunOperator operator) {
        this.operator = operator;
    }

    @Override
    public Resource getResource(String path) {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    @Override
    public Config getConfig() {
        return new Config(operator.config(), ImmutableMap.of())
                .overrideBy(new Config(configMap));
    }

    @Override
    public Long getTaskRunId() {
        return taskRunId;
    }

    public List<DataStore> getInlets() {
        return inlets;
    }

    public List<DataStore> getOutlets() {
        return outlets;
    }

}
