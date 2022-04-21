package com.miotech.kun.workflow.executor.config;

import java.util.List;

public class DispatchExecutorConfig {

    private String kind;
    private String defaultExecutor;
    private List<ExecutorConfig> executorConfigList;

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public List<ExecutorConfig> getExecutorConfigList() {
        return executorConfigList;
    }

    public void setExecutorConfigList(List<ExecutorConfig> executorConfigList) {
        this.executorConfigList = executorConfigList;
    }

    public String getDefaultExecutor() {
        return defaultExecutor;
    }

    public void setDefaultExecutor(String defaultExecutor) {
        this.defaultExecutor = defaultExecutor;
    }
}
