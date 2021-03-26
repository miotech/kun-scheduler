package com.miotech.kun.workflow.executor.kubernetes;

import com.miotech.kun.commons.testing.GuiceTestBase;
import com.miotech.kun.workflow.core.model.common.WorkerInstance;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.junit.Before;
import org.junit.Test;

import javax.inject.Inject;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

public class PodEventMonitorTest extends GuiceTestBase {

    @Inject
    private PodEventMonitor podEventMonitor;

    @Override
    protected void configuration() {
        KubernetesClient client = mock(KubernetesClient.class);
        bind(KubernetesClient.class,client);
    }

    @Before
    private void init(){
        Pod pod = new Pod();
        doReturn()
    }

    @Test
    public void pollingEventTest(){
        podEventMonitor.register()
    }

    private void prepareRegisterInstance(){
        WorkerInstance instance = new WorkerInstance()
    }
}
