package com.miotech.kun.workflow.executor.kubernetes;

import com.miotech.kun.workflow.core.StorageManager;
import io.fabric8.kubernetes.client.KubernetesClient;

public interface PodLifeCycleManagerFactory {
    PodLifeCycleManager create(KubernetesClient kubernetesClient, KubernetesResourceManager kubernetesResourceManager,
                               PodEventMonitor podEventMonitor, String name, StorageManager storageManager);
}
