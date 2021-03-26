package com.miotech.kun.workflow.executor.kubernetes;

import com.miotech.kun.commons.testing.GuiceTestBase;
import com.miotech.kun.workflow.core.model.common.WorkerInstance;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.junit.Before;
import org.junit.Test;

import javax.inject.Inject;

import static com.miotech.kun.workflow.executor.kubernetes.KubernetesConstants.KUN_TASK_ATTEMPT_ID;
import static com.miotech.kun.workflow.executor.kubernetes.KubernetesConstants.KUN_WORKFLOW;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

public class PodEventMonitorTest extends GuiceTestBase {

    @Inject
    private PodEventMonitor podEventMonitor;

    @Override
    protected void configuration() {
        KubernetesClient client = mock(KubernetesClient.class);
        doReturn(MockPodFactory.create()).when(client).pods().withoutLabel(KUN_WORKFLOW).withLabel(KUN_TASK_ATTEMPT_ID,)
        bind(KubernetesClient.class,client);
    }

    @Before
    private void init(){
        Pod pod = new Pod();
    }

    @Test
    public void pollingEventTest(){
        podEventMonitor.register()
    }

    private void prepareRegisterInstance(){
        WorkerInstance instance = new WorkerInstance()
    }
}
