package com.miotech.kun.workflow.executor.kubernetes;

import com.miotech.kun.workflow.executor.config.ExecutorConfig;

public class KubeExecutorConfig extends ExecutorConfig {

    private KubeConfig kubeConfig;

    private ImageHub privateHub;

    public KubeConfig getKubeConfig() {
        return kubeConfig;
    }

    public void setKubeConfig(KubeConfig kubeConfig) {
        this.kubeConfig = kubeConfig;
    }

    public ImageHub getPrivateHub() {
        return privateHub;
    }

    public void setPrivateHub(ImageHub privateHub) {
        this.privateHub = privateHub;
    }

}
