package com.miotech.kun.workflow.web.module;

import com.miotech.kun.commons.utils.Props;
import com.miotech.kun.commons.web.module.AppModule;
import com.miotech.kun.workflow.core.Executor;
import com.miotech.kun.workflow.executor.AbstractQueueManager;
import com.miotech.kun.workflow.executor.WorkerLifeCycleManager;
import com.miotech.kun.workflow.executor.WorkerMonitor;
import com.miotech.kun.workflow.executor.kubernetes.KubernetesExecutor;
import com.miotech.kun.workflow.executor.kubernetes.KubernetesResourceManager;
import com.miotech.kun.workflow.executor.kubernetes.PodEventMonitor;
import com.miotech.kun.workflow.executor.kubernetes.PodLifeCycleManager;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        KubernetesClient client = new DefaultKubernetesClient(kubernetesConfig());
        bind(KubernetesClient.class).toInstance(client);
        bind(AbstractQueueManager.class).to(KubernetesResourceManager.class);
        bind(WorkerMonitor.class).to(PodEventMonitor.class);
        bind(WorkerLifeCycleManager.class).to(PodLifeCycleManager.class);
        bind(Executor.class).to(KubernetesExecutor.class);
    }

    private Config kubernetesConfig() {
        String masterUrl = props.get("executor.env.url");
        String oauthToken = props.get("executor.env.oauthToken");
        String caCertFile = props.get("executor.env.caCertFile");

        if (masterUrl != null && oauthToken != null && caCertFile != null) {
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
