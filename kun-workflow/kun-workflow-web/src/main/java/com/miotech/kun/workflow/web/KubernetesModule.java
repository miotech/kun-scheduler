package com.miotech.kun.workflow.web;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.miotech.kun.commons.utils.Props;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;

public class KubernetesModule extends AbstractModule {
    private final Props props;

    public KubernetesModule(Props props){
        this.props = props;
    }

    @Provides
    public KubernetesClient getKubernetesClient(){
        Config config = new ConfigBuilder().withMasterUrl(props.get("executor.env.host")).build();
        KubernetesClient client = new DefaultKubernetesClient(config);
        return client;
    }
}
