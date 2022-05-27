package com.miotech.kun.workflow.executor.config;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.miotech.kun.workflow.core.model.resource.ResourceQueue;
import com.miotech.kun.workflow.executor.kubernetes.KubeExecutorConfig;

import java.util.List;
import java.util.Map;

@JsonTypeInfo(use=JsonTypeInfo.Id.NAME, include=JsonTypeInfo.As.EXISTING_PROPERTY,property = "kind",visible = true)
@JsonSubTypes(value = {
        @JsonSubTypes.Type(value = KubeExecutorConfig.class, name = "kubernetes"),
        @JsonSubTypes.Type(value = ExecutorConfig.class, name = "local"),
})
public class ExecutorConfig {
    private String name;
    private String kind;
    private Map<String, String> storage;
    private List<ResourceQueue> resourceQueues;
    private ExecutorRpcConfig executorRpcConfig;
    private String label;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, String> getStorage() {
        return storage;
    }

    public void setStorage(Map<String, String> storage) {
        this.storage = storage;
    }

    public List<ResourceQueue> getResourceQueues() {
        return resourceQueues;
    }

    public void setResourceQueues(List<ResourceQueue> resourceQueues) {
        this.resourceQueues = resourceQueues;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public ExecutorRpcConfig getExecutorRpcConfig() {
        return executorRpcConfig;
    }

    public void setExecutorRpcConfig(ExecutorRpcConfig executorRpcConfig) {
        this.executorRpcConfig = executorRpcConfig;
    }
}
