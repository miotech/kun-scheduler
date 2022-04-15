package com.miotech.kun.workflow.executor.kubernetes;

import io.fabric8.kubernetes.client.Config;

import java.util.Map;

public class KubeExecutorConfig {

    private final Config k8sClientConfig;
    private final Map<String, String> storageConfig;

    public KubeExecutorConfig(Config k8sClientConfig, Map<String, String> storageConfig) {
        this.k8sClientConfig = k8sClientConfig;
        this.storageConfig = storageConfig;
    }

    public Config getK8sClientConfig() {
        return k8sClientConfig;
    }

    public Map<String, String> getStorageConfig() {
        return storageConfig;
    }

    public static KubeExecutorConfigBuilder newBuilder() {
        return new KubeExecutorConfigBuilder();
    }


    public static final class KubeExecutorConfigBuilder {
        private Config k8sClientConfig;
        private Map<String, String> storageConfig;

        private KubeExecutorConfigBuilder() {
        }

        public KubeExecutorConfigBuilder withK8sClientConfig(Config k8sClientConfig) {
            this.k8sClientConfig = k8sClientConfig;
            return this;
        }

        public KubeExecutorConfigBuilder withStorageConfig(Map<String, String> storageConfig) {
            this.storageConfig = storageConfig;
            return this;
        }

        public KubeExecutorConfig build() {
            return new KubeExecutorConfig(k8sClientConfig, storageConfig);
        }
    }
}
