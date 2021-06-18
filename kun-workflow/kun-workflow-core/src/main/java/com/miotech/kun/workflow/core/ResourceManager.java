package com.miotech.kun.workflow.core;

import com.miotech.kun.workflow.core.model.resource.ResourceQueue;
import com.miotech.kun.workflow.core.model.task.TaskPriority;

public interface ResourceManager {

    public void changePriority(long taskAttemptId, String queueName, TaskPriority priority);

    public ResourceQueue createResourceQueue(ResourceQueue resourceQueue);

    public ResourceQueue updateResourceQueue(ResourceQueue resourceQueue);
}
