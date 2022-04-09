package com.miotech.kun.workflow.executor.kubernetes;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.assistedinject.Assisted;
import com.miotech.kun.commons.utils.Props;
import com.miotech.kun.workflow.core.model.resource.ResourceQueue;
import com.miotech.kun.workflow.executor.AbstractQueueManager;
import com.miotech.kun.workflow.executor.TaskAttemptQueue;
import com.miotech.kun.workflow.executor.local.MiscService;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.miotech.kun.workflow.executor.kubernetes.KubernetesConstants.KUN_WORKFLOW;
import static com.miotech.kun.workflow.executor.kubernetes.KubernetesConstants.TASK_QUEUE;


public class KubernetesResourceManager extends AbstractQueueManager {

    private final Logger logger = LoggerFactory.getLogger(KubernetesResourceManager.class);
    private KubernetesClient client;
    private final String name;

    @Inject
    public KubernetesResourceManager(@Assisted KubernetesClient kubernetesClient, Props props, MiscService miscService,
                                     EventBus eventBus, @Assisted String name) {
        super(props, miscService, eventBus, name);
        this.client = kubernetesClient;
        this.name = name;
        logger.info("k8s resource manager: {} initializing", name);
    }

    @Override
    public Integer getCapacity(TaskAttemptQueue taskAttemptQueue) {
        ResourceQueue limitResource = taskAttemptQueue.getResourceQueue();
        ResourceQueue usedResource = getUsedResource(taskAttemptQueue.getName());
        logger.debug("{} k8s resources manager queue = {},has {} running pod ,limit = {}", name, taskAttemptQueue.getName(), usedResource.getWorkerNumbers(), limitResource.getWorkerNumbers());
        return limitResource.getWorkerNumbers() - usedResource.getWorkerNumbers() ;
    }

    @Override
    public ResourceQueue createResourceQueue(ResourceQueue resourceQueue) {
        throw new UnsupportedOperationException("kubernetes executor not support create resource queue currently");
    }

    @Override
    public ResourceQueue updateResourceQueue(ResourceQueue resourceQueue) {
        throw new UnsupportedOperationException("kubernetes executor not support update resource queue currently");
    }

    private ResourceQueue getUsedResource(String queueName) {
        PodList podList = client.pods()
                .inNamespace(props.getString("executor.env."+name+".namespace"))
                .withLabel(KUN_WORKFLOW)
                .withLabel(TASK_QUEUE, queueName)
                .list();
        Integer runningPod = podList.getItems().size();
        return ResourceQueue.newBuilder()
                .withQueueName(queueName)
                .withWorkerNumbers(runningPod)
                .build();
    }
}
