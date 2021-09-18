package com.miotech.kun.workflow.core;

import com.miotech.kun.workflow.core.model.resource.ResourceQueue;

public interface ResourceManager {

    public void changePriority(long taskAttemptId, String queueName, Integer priority);

    public ResourceQueue createResourceQueue(ResourceQueue resourceQueue);

    public ResourceQueue updateResourceQueue(ResourceQueue resourceQueue);
}
