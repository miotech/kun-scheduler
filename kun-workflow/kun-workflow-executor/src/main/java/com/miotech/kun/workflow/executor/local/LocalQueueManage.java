package com.miotech.kun.workflow.executor.local;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.miotech.kun.commons.utils.Props;
import com.miotech.kun.workflow.core.model.resource.ResourceQueue;
import com.miotech.kun.workflow.executor.AbstractQueueManager;
import com.miotech.kun.workflow.executor.TaskAttemptQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;


@Singleton
public class LocalQueueManage extends AbstractQueueManager {

    private static Logger logger = LoggerFactory.getLogger(LocalQueueManage.class);

    private final LocalProcessBackend localProcessBackend;


    @Inject
    public LocalQueueManage(Props props, MiscService miscService, LocalProcessBackend localProcessBackend) {
        super(props, miscService);
        this.localProcessBackend = localProcessBackend;
    }

    @Override
    public Integer getCapacity(TaskAttemptQueue taskAttemptQueue) {
        ResourceQueue limitResource = taskAttemptQueue.getResourceQueue();
        ResourceQueue usedResource = getUsedResource(taskAttemptQueue.getName());
        logger.debug("queue = {},has {} running process ,limit = {}", taskAttemptQueue.getName(), usedResource.getWorkerNumbers(), limitResource.getWorkerNumbers());
        return limitResource.getWorkerNumbers() - usedResource.getWorkerNumbers();
    }

    @Override
    public ResourceQueue createResourceQueue(ResourceQueue resourceQueue) {
        throw new UnsupportedOperationException("local executor not support create resource queue yet");
    }

    @Override
    public ResourceQueue updateResourceQueue(ResourceQueue resourceQueue) {
        throw new UnsupportedOperationException("local executor not support create resource queue yet");
    }

    private ResourceQueue getUsedResource(String queueName) {
        List<ProcessSnapShot> runningProcess = localProcessBackend.fetchRunningProcess(queueName);
        return ResourceQueue.newBuilder()
                .withQueueName(queueName)
                .withWorkerNumbers(runningProcess.size())
                .build();
    }


}
