package com.miotech.kun.workflow.web.module;

import com.miotech.kun.commons.utils.Props;
import com.miotech.kun.commons.web.module.AppModule;
import com.miotech.kun.workflow.core.Executor;
import com.miotech.kun.workflow.executor.AbstractQueueManager;
import com.miotech.kun.workflow.executor.WorkerLifeCycleManager;
import com.miotech.kun.workflow.executor.WorkerMonitor;
import com.miotech.kun.workflow.executor.kubernetes.*;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

public class KubernetesExecutorModule extends AppModule {

    private static final Logger logger = LoggerFactory.getLogger(KubernetesExecutorModule.class);

    private final Props props;

    public KubernetesExecutorModule(Props props) {
        super(props);
        this.props = props;
    }

    @Override
    protected void configure() {
        super.configure();
        String storagePrefix = "executor.env.storage";
        KubeExecutorConfig kubeExecutorConfig = props.getValue("executor.config",KubeExecutorConfig.class);
        bind(KubeExecutorConfig.class).toInstance(kubeExecutorConfig);
        bind(AbstractQueueManager.class).to(KubernetesResourceManager.class);
        bind(WorkerMonitor.class).to(PodEventMonitor.class);
        bind(WorkerLifeCycleManager.class).to(PodLifeCycleManager.class);
        bind(Executor.class).to(KubernetesExecutor.class);
    }

    private Config kubernetesConfig() {
        String masterUrl = props.get("executor.env.url");
        String oauthToken = props.get("executor.env.oauthToken");
        String caCertFile = props.get("executor.env.caCertFile");

        if (isNotEmpty(masterUrl) && isNotEmpty(oauthToken) && isNotEmpty(caCertFile)) {
            logger.info("Interact with k8s cluster using oauth authentication.");
            return new ConfigBuilder()
                    .withMasterUrl(masterUrl)
                    .withOauthToken(oauthToken)
                    .withCaCertFile(caCertFile)
                    .build();
        } else {
            logger.info("Interact with k8s cluster using rbac authentication.");
            return Config.autoConfigure(null);
        }
    }

}
