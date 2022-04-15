package com.miotech.kun.workflow.executor.kubernetes;

public interface KubernetesExecutorFactory {
    KubernetesExecutor create(KubeExecutorConfig kubeExecutorConfig, String name);
}
