package com.miotech.kun.workflow.executor.kubernetes;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.miotech.kun.workflow.core.Executor;
import com.miotech.kun.workflow.core.model.resource.ResourceQueue;
import com.miotech.kun.workflow.core.model.task.TaskPriority;
import com.miotech.kun.workflow.core.model.taskrun.TaskAttempt;
import com.miotech.kun.workflow.executor.AbstractQueueManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class KubernetesExecutor implements Executor {

    private final Logger logger = LoggerFactory.getLogger(KubernetesExecutor.class);
    private final WorkerLifeCycleManager podLifeCycleManager;//pod生命周期管理
    private final AbstractQueueManager kubernetesResourceManager;//管理kubernetes资源配额

    @Inject
    public KubernetesExecutor(PodLifeCycleManager podLifeCycleManager, KubernetesResourceManager kubernetesResourceManager) {
        this.podLifeCycleManager = podLifeCycleManager;
        this.kubernetesResourceManager = kubernetesResourceManager;
    }


    public boolean submit(TaskAttempt taskAttempt) {
        logger.info("submit taskAttemptId = {} to executor", taskAttempt.getId());
        podLifeCycleManager.start(taskAttempt);
        return true;
    }


    @Override
    public boolean reset() {
        logger.info("kubernetes going to reset");
        podLifeCycleManager.reset();
        return true;
    }

    @Override
    public boolean recover() {
        logger.info("kubernetes going to recover");
        podLifeCycleManager.recover();
        return true;
    }

    @Override
    public String workerLog(Long taskAttemptId, Integer tailLines) {
        return podLifeCycleManager.getWorkerLog(taskAttemptId, tailLines);
    }

    @Override
    public boolean cancel(Long taskAttemptId) {
        podLifeCycleManager.stop(taskAttemptId);
        return true;
    }

    @Override
    public void changePriority(long taskAttemptId, String queueName, TaskPriority priority) {
        kubernetesResourceManager.changePriority(taskAttemptId, queueName, priority);
    }

    @Override
    public ResourceQueue createResourceQueue(ResourceQueue resourceQueue) {
        return kubernetesResourceManager.createResourceQueue(resourceQueue);
    }

    public ResourceQueue updateResourceQueue(ResourceQueue resourceQueue) {
        return kubernetesResourceManager.updateResourceQueue(resourceQueue);
    }

}
