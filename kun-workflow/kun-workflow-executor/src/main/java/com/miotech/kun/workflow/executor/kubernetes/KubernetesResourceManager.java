package com.miotech.kun.workflow.executor.kubernetes;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.miotech.kun.commons.utils.Props;
import com.miotech.kun.workflow.core.ResourceManager;
import com.miotech.kun.workflow.core.model.resource.ResourceQueue;
import com.miotech.kun.workflow.core.model.task.TaskPriority;
import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static com.miotech.kun.workflow.executor.kubernetes.KubernetesConstants.*;

@Singleton
public class KubernetesResourceManager implements ResourceManager {

    private final Logger logger = LoggerFactory.getLogger(ResourceManager.class);
    private KubernetesClient client;
    private Props props;

    @Inject
    public KubernetesResourceManager(KubernetesClient client, Props props) {
        this.client = client;
        this.props = props;
    }

    //todo:支持资源隔离
    public void init() {//根据配置文件初始化nameSpace资源配额
        List<ResourceQueue> resourceQueueList = loadResourceQueue();
        for (ResourceQueue resourceQueue : resourceQueueList) {
            createResourceQueue(resourceQueue);
        }
    }

    public List<ResourceQueue> loadResourceQueue() {
        List<String> queueNames = props.getStringList("executor.env.resourceQueues");
        List<ResourceQueue> queueList = new ArrayList<>();
        for (String queueName : queueNames) {
            String prefix = "executor.env.resourceQueues." + queueName;
            Integer cores = props.getInt(prefix + ".quota.cores", 0);
            Integer memory = props.getInt(prefix + ".quota.memory", 0);
            Integer workerNumbers = props.getInt(prefix + ".quota.workerNumbers", 0);
            ResourceQueue resourceQueue = ResourceQueue.newBuilder()
                    .withQueueName(queueName)
                    .withCores(cores)
                    .withMemory(memory)
                    .withWorkerNumbers(workerNumbers)
                    .build();
            logger.info("init queue = {}", resourceQueue);
            queueList.add(resourceQueue);
        }
        return queueList;
    }


    /**
     * kubernetes version less than 1.15 disablePreemption is not support
     *
     * @param taskAttemptId
     * @param queueName
     * @param taskPriority
     * @return
     */
    @Override
    public void changePriority(long taskAttemptId, String queueName, TaskPriority taskPriority) {
        Pod pod = client
                .pods()
                .inNamespace(props.getString("executor.env.namespace"))
                .withName(KUN_WORKFLOW + taskAttemptId)
                .get();
        if (pod == null) {
            throw new IllegalStateException("can not find pod with taskAttemptId = {}" + taskAttemptId);
        }
        if (!pod.getStatus().getPhase().toLowerCase().equals("pending")) {
            logger.warn("pod with taskAttemptId = {} is not in pending", taskAttemptId);
            return;
        }
        Pod newPod = new PodBuilder()
                .withMetadata(pod.getMetadata())
                .withNewSpecLike(pod.getSpec())
                .withNewPriorityClassName(taskPriority.name().toLowerCase())
                .endSpec()
                .build();
        client.pods().inNamespace(props.getString("executor.env.namespace")).createOrReplace(newPod);
    }

    @Override
    public ResourceQueue createResourceQueue(ResourceQueue resourceQueue) {
        createNameSpaceIfNotExist(resourceQueue.getQueueName());

        //config quota
        ResourceQuota resourceQuota = new ResourceQuotaBuilder()
                .withNewMetadata()
                .withName(resourceQueue.getQueueName())
                .withNamespace(resourceQueue.getQueueName())
                .endMetadata()
                .withNewSpec()
                .addToHard("limits.cpu", new Quantity(resourceQueue.getCores().toString()))
                .addToHard("limits.memory", new Quantity(resourceQueue.getMemory().toString(), "Gi"))
                .addToHard("pods", new Quantity(resourceQueue.getWorkerNumbers().toString()))
                .endSpec()
                .build();
        client.resourceQuotas()
                .inNamespace(resourceQueue.getQueueName())
                .createOrReplace(resourceQuota);
        return resourceQueue;
    }

    @Override
    public ResourceQueue updateResourceQueue(ResourceQueue resourceQueue) {
        ResourceQuota resourceQuota = new ResourceQuotaBuilder()
                .withNewMetadata()
                .withName(resourceQueue.getQueueName())
                .withNamespace(resourceQueue.getQueueName())
                .endMetadata()
                .withNewSpec()
                .addToHard("limits.cpu", new Quantity(resourceQueue.getCores().toString()))
                .addToHard("limits.memory", new Quantity(resourceQueue.getMemory().toString(), "Gi"))
                .addToHard("pods", new Quantity(resourceQueue.getWorkerNumbers().toString()))
                .endSpec()
                .build();
        client.resourceQuotas().inNamespace(resourceQueue.getQueueName()).createOrReplace(resourceQuota);
        return resourceQueue;
    }

    private void configLimitRange() {
        //config limit range
        LimitRange limitRange = new LimitRangeBuilder()
                .withNewMetadata()
                .withName("podlimitrange")
                .withNamespace(props.getString("executor.env.namespace"))
                .endMetadata()
                .withNewSpec()
                .addToLimits(new LimitRangeItemBuilder()
                        .withType("Container")
                        .addToDefault("cpu", new Quantity(CPU_DEFAULT))
                        .addToDefaultRequest("cpu", new Quantity(CPU_REQUEST))
                        .addToDefault("memory", new Quantity(MEMORY_DEFAULT, "Mi"))
                        .addToDefaultRequest("memory", new Quantity(MEMORY_REQUEST, "Mi"))
                        .build())
                .endSpec()
                .build();
        client.limitRanges()
                .inNamespace(props.getString("executor.env.namespace"))
                .createOrReplace(limitRange);
    }


    /**
     * @param name
     * @return
     */
    private void createNameSpaceIfNotExist(String name) {
        Namespace existNameSpace = client.namespaces().withName(name).get();
        if (existNameSpace != null) {
            return;
        }
        Namespace namespace = new NamespaceBuilder()
                .withNewMetadata()
                .withName(name)
                .endMetadata()
                .build();
        client.namespaces().create(namespace);
    }
}
