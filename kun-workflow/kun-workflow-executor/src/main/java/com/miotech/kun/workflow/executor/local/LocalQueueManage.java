package com.miotech.kun.workflow.executor.local;

import com.miotech.kun.workflow.core.model.resource.ResourceQueue;
import com.miotech.kun.workflow.executor.AbstractQueueManager;
import com.miotech.kun.workflow.executor.TaskAttemptQueue;
import com.miotech.kun.workflow.executor.config.ExecutorConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;


public class LocalQueueManage extends AbstractQueueManager {

    private static Logger logger = LoggerFactory.getLogger(LocalQueueManage.class);

    private final LocalProcessBackend localProcessBackend;


    public LocalQueueManage(ExecutorConfig executorConfig, LocalProcessBackend localProcessBackend) {
        super(executorConfig, "local");
        this.localProcessBackend = localProcessBackend;
    }

    @Override
    public Integer getCapacity(TaskAttemptQueue taskAttemptQueue) {
        ResourceQueue limitResource = taskAttemptQueue.getResourceQueue();
        ResourceQueue usedResource = getUsedResource(taskAttemptQueue.getName());
        //logger.debug("queue = {},has {} running process ,limit = {}", taskAttemptQueue.getName(), usedResource.getWorkerNumbers(), limitResource.getWorkerNumbers());
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
