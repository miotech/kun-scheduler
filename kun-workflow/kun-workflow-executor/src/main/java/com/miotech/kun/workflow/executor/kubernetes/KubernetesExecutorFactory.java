package com.miotech.kun.workflow.executor.kubernetes;

import io.fabric8.kubernetes.client.KubernetesClient;

public interface KubernetesExecutorFactory {
    KubernetesExecutor create(KubernetesClient kubernetesClient, String name);
}
