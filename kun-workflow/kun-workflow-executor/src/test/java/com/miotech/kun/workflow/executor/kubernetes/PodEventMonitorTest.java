package com.miotech.kun.workflow.executor.kubernetes;

import com.miotech.kun.commons.testing.GuiceTestBase;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.junit.Test;

public class PodEventMonitorTest extends GuiceTestBase {

    @Override
    protected void configuration() {
        KubernetesClient client = mock(KubernetesClient.class);
    }

    @Test
    public void pollingEventTest(){

    }
}
