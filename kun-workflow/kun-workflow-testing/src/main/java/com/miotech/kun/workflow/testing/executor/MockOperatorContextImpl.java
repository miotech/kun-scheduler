package com.miotech.kun.workflow.testing.executor;

import com.google.common.collect.ImmutableMap;
import com.miotech.kun.metadata.core.model.dataset.DataStore;
import com.miotech.kun.workflow.core.execution.Config;
import com.miotech.kun.workflow.core.execution.OperatorContext;
import com.miotech.kun.workflow.core.model.executetarget.ExecuteTarget;
import com.miotech.kun.workflow.core.resource.Resource;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MockOperatorContextImpl implements OperatorContext {

    private Config config;
    private final Long taskRunId;
    private final ExecuteTarget executeTarget;
    private List<DataStore> inlets = Collections.emptyList();
    private List<DataStore> outlets = Collections.emptyList();
    private String queueName = "default";

    public MockOperatorContextImpl(Config config, Long taskRunId, ExecuteTarget executeTarget) {
        this.config = config;
        this.taskRunId = taskRunId;
        this.executeTarget = executeTarget;
    }

    public void setParam(String key, String value) {
        Map<String,Object> params = new HashMap<>(config.getValues());
        params.put(key,value);
        this.config = new Config(params);
    }

    public void overwriteConfig(Config config) {
        this.config = this.config.overrideBy(config);
    }

    @Override
    public Resource getResource(String path) {
        throw new UnsupportedOperationException("Not implemented yet.");
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
    public String getScheduleTime() {
        return "000000000000";
    }

    @Override
    public ExecuteTarget getExecuteTarget() {
        return ExecuteTarget.newBuilder().withName("test").withProperties(ImmutableMap.of("schema", "test")).build();
    }

    @Override
    public String getQueueName() {
        return queueName;
    }

    public List<DataStore> getInlets() {
        return inlets;
    }

    public List<DataStore> getOutlets() {
        return outlets;
    }

}
