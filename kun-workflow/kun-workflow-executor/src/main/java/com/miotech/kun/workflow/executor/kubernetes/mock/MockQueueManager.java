package com.miotech.kun.workflow.executor.kubernetes.mock;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.miotech.kun.commons.utils.Props;
import com.miotech.kun.workflow.core.model.resource.ResourceQueue;
import com.miotech.kun.workflow.core.model.taskrun.TaskAttempt;
import com.miotech.kun.workflow.executor.AbstractQueueManager;
import com.miotech.kun.workflow.executor.TaskAttemptQueue;
import com.miotech.kun.workflow.executor.local.MiscService;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class MockQueueManager extends AbstractQueueManager {

    Map<String, Integer> map = new ConcurrentHashMap<>();

    @Inject
    public MockQueueManager(Props props, MiscService miscService) {
        super(props, miscService);
    }

    @Override
    public boolean hasCapacity(TaskAttemptQueue taskAttemptQueue) {
        ResourceQueue limitResource = taskAttemptQueue.getResourceQueue();
        if(!map.containsKey(taskAttemptQueue.getName())){
            map.put(taskAttemptQueue.getName(),0);
            return false;
        }
        return map.get(taskAttemptQueue.getName()) < limitResource.getWorkerNumbers();
    }

    @Override
    public ResourceQueue createResourceQueue(ResourceQueue resourceQueue) {
        return null;
    }

    @Override
    public ResourceQueue updateResourceQueue(ResourceQueue resourceQueue) {
        return null;
    }

    public void addWorker(TaskAttempt taskAttempt) {
        Integer runningPod = map.get(taskAttempt.getQueueName());
        map.put(taskAttempt.getQueueName(), runningPod + 1);
    }

    public void removeWorker(TaskAttempt taskAttempt) {
        Integer runningPod = map.get(taskAttempt.getQueueName());
        map.put(taskAttempt.getQueueName(), runningPod - 1);
    }
}
