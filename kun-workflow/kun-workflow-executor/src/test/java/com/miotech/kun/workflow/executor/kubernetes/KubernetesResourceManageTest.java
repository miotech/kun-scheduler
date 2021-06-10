package com.miotech.kun.workflow.executor.kubernetes;

import com.google.inject.Inject;
import com.miotech.kun.commons.utils.Props;
import com.miotech.kun.workflow.core.model.resource.ResourceQueue;
import com.miotech.kun.workflow.executor.CommonTestBase;
import io.fabric8.kubernetes.api.model.Namespace;
import io.fabric8.kubernetes.api.model.Quantity;
import io.fabric8.kubernetes.api.model.ResourceQuota;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@Ignore
public class KubernetesResourceManageTest extends CommonTestBase {


    @Inject
    private KubernetesResourceManager kubernetesResourceManager;

    private KubernetesClient client;


    @Override
    protected String getFlywayLocation() {
        return "workflow/";
    }

    @Override
    protected void configuration() {
        Props props = new Props();
        props.put("executor.env.name", "KUBERNETES");
        props.put("executor.env.version", "1.15");
        props.put("executor.env.logPath", "/server/lib/logs");
        props.put("executor.env.resourceQueues", "default,test");
        props.put("executor.env.resourceQueues.default.quota.cores", 2);
        props.put("executor.env.resourceQueues.default.quota.memory", 6);
        props.put("executor.env.resourceQueues.default.quota.workerNumbers", 3);
        props.put("executor.env.resourceQueues.test.quota.cores", 2);
        props.put("executor.env.resourceQueues.test.quota.memory", 1);
        props.put("executor.env.resourceQueues.test.quota.workerNumbers", 3);
        Config config = new ConfigBuilder().withMasterUrl("http://localhost:9880").build();
        client = new DefaultKubernetesClient(config);
        bind(KubernetesClient.class, client);
        bind(Props.class, props);

        super.configuration();
    }

    @Test
    public void testCreateNameSpaceAfterSetup() {
        kubernetesResourceManager.init();
        List<Namespace> nameSpaces = client.namespaces().list().getItems();
        List<String> foundNameSpace = nameSpaces.stream().
                map(namespace -> namespace.getMetadata().getName())
                .collect(Collectors.toList());

        //verify
        assertThat(foundNameSpace, hasItems("default","test"));
        ResourceQuota defaultQuota = client.resourceQuotas().inNamespace("default").withName("default").get();
        ResourceQueue defaultQueue = coverQuotaToQueue(defaultQuota);
        assertThat(defaultQueue.getCores(), is(2));
        assertThat(defaultQueue.getMemory(), is(6));
        assertThat(defaultQueue.getWorkerNumbers(), is(3));

        ResourceQuota testQuota = client.resourceQuotas().inNamespace("test").withName("test").get();
        ResourceQueue testQueue = coverQuotaToQueue(testQuota);
        assertThat(testQueue.getCores(), is(2));
        assertThat(testQueue.getMemory(), is(1));
        assertThat(testQueue.getWorkerNumbers(), is(3));

        //clean up
        client.resourceQuotas().inNamespace("default").delete();
        client.resourceQuotas().inNamespace("test").delete();


    }

    @Test
    public void testCreateResourceQueue() {
        ResourceQueue toCreate = ResourceQueue.newBuilder()
                .withQueueName("queue-create")
                .withCores(2)
                .withMemory(2)
                .withWorkerNumbers(2)
                .build();
        kubernetesResourceManager.createResourceQueue(toCreate);
        ResourceQuota createdQuota = client.resourceQuotas().inNamespace("queue-create").withName("queue-create").get();
        ResourceQueue createdQueue = coverQuotaToQueue(createdQuota);
        assertThat(createdQueue, is(toCreate));

        //clean up
        client.resourceQuotas().inNamespace("queue-create").delete();

    }

    @Test
    public void testUpdateResourceQueue() {
        ResourceQueue toCreate = ResourceQueue.newBuilder()
                .withQueueName("queue-create")
                .withCores(2)
                .withMemory(2)
                .withWorkerNumbers(2)
                .build();
        kubernetesResourceManager.createResourceQueue(toCreate);

        ResourceQueue toUpdate = toCreate
                .cloneBuilder()
                .withCores(3)
                .withMemory(3)
                .withWorkerNumbers(3)
                .build();

        kubernetesResourceManager.updateResourceQueue(toUpdate);
        ResourceQuota updatedQuota = client.resourceQuotas().inNamespace("queue-create").withName("queue-create").get();
        ResourceQueue updatedQueue = coverQuotaToQueue(updatedQuota);
        assertThat(updatedQueue, is(toUpdate));

        //cleanUp
        client.resourceQuotas().inNamespace("queue-create").delete();
    }

    private ResourceQueue coverQuotaToQueue(ResourceQuota resourceQuota) {
        Map<String, Quantity> hards = resourceQuota.getSpec().getHard();
        return ResourceQueue.newBuilder()
                .withQueueName(resourceQuota.getMetadata().getName())
                .withCores(Integer.parseInt(hards.get("limits.cpu").getAmount()))
                .withMemory(Integer.parseInt(hards.get("limits.memory").getAmount()))
                .withWorkerNumbers(Integer.parseInt(hards.get("pods").getAmount()))
                .build();
    }

}
