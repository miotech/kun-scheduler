package com.miotech.kun.workflow.operator;

import com.miotech.kun.workflow.testing.executor.OperatorRunner;
import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.api.model.batch.JobBuilder;
import io.fabric8.kubernetes.client.server.mock.KubernetesServer;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static com.miotech.kun.workflow.operator.DockerConfiguration.*;
import static org.junit.Assert.assertTrue;

public class KubernetesOperatorTest {
    private static final String TEST_JOB_NAME = "test-job";

    private OperatorRunner operatorRunner;

    @Rule
    public KubernetesServer server = new KubernetesServer();

    @Before
    public void init(){
        KubernetesOperator operator = new KubernetesOperator();
        operator.setClient(server.getClient());
        operatorRunner = new OperatorRunner(operator);
        operatorRunner.setConfigKey(CONF_K8S_JOB_NAME, TEST_JOB_NAME);
    }

    @Test
    public void run() {
        operatorRunner.setConfigKey(CONF_CONTAINER_IMAGE, "ubuntu");
        operatorRunner.setConfigKey(CONF_CONTAINER_NAME, "test");
        operatorRunner.setConfigKey(CONF_CONTAINER_COMMAND, "echo {{a}}");
        operatorRunner.setConfigKey(CONF_VARIABLES, "{\"a\": \"hello-world\"}");

        String namespace = "default";
        server.expect().withPath("/api/v1/namespaces/default/configmaps")
                .andReturn(200, new ConfigMapBuilder()
                        .build())
                .once();

        server.expect().withPath("/apis/batch/v1/namespaces/default/jobs")
                .andReturn(200, new JobBuilder()
                        .withNewMetadata()
                        .withNamespace(namespace)
                        .endMetadata()
                        .build())
                .once();

        server.expect().withPath("/apis/batch/v1/namespaces/default/jobs/" + TEST_JOB_NAME)
                .andReturn(200, new JobBuilder()
                        .withNewMetadata()
                        .withNamespace(namespace)
                        .endMetadata()
                        .withNewStatus()
                        .withCompletionTime("2020-12-20T10:10:10Z")
                        .withSucceeded(1)
                        .endStatus()
                        .build())
                .once();

        PodStatus podStatus = new PodStatusBuilder()
                .withContainerStatuses(
                        new ContainerStatusBuilder()
                                .editOrNewState()
                                .withNewTerminated()
                                .withMessage("")
                                .endTerminated()
                                .endState()
                        .build()
                )
                .withConditions(new PodConditionBuilder()
                        .withType("Ready")
                        .withStatus("True")
                        .build())
                .build();
        String podName = "pod-" + TEST_JOB_NAME;
        server.expect().withPath("/api/v1/namespaces/default/pods?labelSelector=" + "job-id%3Dtest-job%2Cproject%3Dkun-workflow%2Coperator%3DKubernetesOperator")
                .andReturn(200, new PodListBuilder()
                        .addNewItem()
                        .withNewMetadata()
                        .withName(podName)
                        .withNamespace(namespace)
                        .endMetadata()
                        .withStatus(podStatus)
                        .endItem()
                        .build())
                .once();
        server.expect().withPath(String.format("/api/v1/namespaces/default/pods/%s/log?pretty=false&timestamps=true" , podName))
                .andReturn(200, "logger inside container - hello-world")
                .once();

        server.expect().withPath("/api/v1/namespaces/default/configmaps/configmap-" + TEST_JOB_NAME)
                .andReturn(200, "true")
                .once();

        server.expect().withPath("/apis/batch/v1/namespaces/default/jobs/" + TEST_JOB_NAME)
                .andReturn(200, "true")
                .once();
        boolean isSuccess = operatorRunner.run();

        assertTrue(isSuccess);
        String logs = String.join("\n", operatorRunner.getLog());
        assertTrue(logs.contains("logger inside container - hello-world"));
    }

    @Test
    public void run_and_abort() {
        operatorRunner.setConfigKey(CONF_CONTAINER_IMAGE, "ubuntu");
        operatorRunner.setConfigKey(CONF_CONTAINER_NAME, "test");
        operatorRunner.setConfigKey(CONF_CONTAINER_COMMAND, "sleep 4m");
        operatorRunner.setConfigKey(CONF_CONTAINER_ENV, "{}");

        String namespace = "default";
        server.expect().withPath("/api/v1/namespaces/default/configmaps")
                .andReturn(200, new ConfigMapBuilder()
                        .build())
                .once();

        server.expect().withPath("/apis/batch/v1/namespaces/default/jobs")
                .andReturn(200, new JobBuilder()
                        .withNewMetadata()
                        .withNamespace(namespace)
                        .endMetadata()
                        .build())
                .once();

        server.expect().withPath("/apis/batch/v1/namespaces/default/jobs/" + TEST_JOB_NAME)
                .andReturn(200, new JobBuilder()
                        .withNewMetadata()
                        .withNamespace(namespace)
                        .endMetadata()
                        .withNewStatus()
                        .endStatus()
                        .build())
                .times(4);

        PodStatus podStatus = new PodStatusBuilder()
                .withContainerStatuses(
                        new ContainerStatusBuilder()
                                .editOrNewState()
                                .withNewTerminated()
                                .withMessage("")
                                .endTerminated()
                                .endState()
                                .build()
                )
                .withConditions(new PodConditionBuilder()
                        .withType("Ready")
                        .withStatus("True")
                        .build())
                .build();
        String podName = "pod-" + TEST_JOB_NAME;
        server.expect().withPath("/api/v1/namespaces/default/pods?labelSelector=" + "job-id%3Dtest-job%2Cproject%3Dkun-workflow%2Coperator%3DKubernetesOperator")
                .andReturn(200, new PodListBuilder()
                        .addNewItem()
                        .withNewMetadata()
                        .withName(podName)
                        .withNamespace(namespace)
                        .endMetadata()
                        .withStatus(podStatus)
                        .endItem()
                        .build())
                .times(4);
        server.expect().withPath(String.format("/api/v1/namespaces/default/pods/%s/log?pretty=false&timestamps=true" , podName))
                .andReturn(200, "logger inside container - ")
                .times(4);


        operatorRunner.abortAfter(4, (operatorContext)->{
            server.expect().withPath("/api/v1/namespaces/default/configmaps/configmap-" + TEST_JOB_NAME)
                    .andReturn(200, "true")
                    .times(2);

            server.expect().withPath("/apis/batch/v1/namespaces/default/jobs/" + TEST_JOB_NAME)
                    .andReturn(200, "true")
                    .times(2);
        });
        operatorRunner.run();

        String logs = String.join("\n", operatorRunner.getLog());
        assertTrue(logs.contains(" Cleanup job resource : namespace - default, job - " + TEST_JOB_NAME));
    }
}