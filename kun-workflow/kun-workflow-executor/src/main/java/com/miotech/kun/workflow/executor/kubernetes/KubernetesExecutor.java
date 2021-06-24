package com.miotech.kun.workflow.executor.kubernetes;

import com.google.common.util.concurrent.Uninterruptibles;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.miotech.kun.workflow.core.Executor;
import com.miotech.kun.workflow.core.model.resource.ResourceQueue;
import com.miotech.kun.workflow.core.model.task.TaskPriority;
import com.miotech.kun.workflow.core.model.taskrun.TaskAttempt;
import com.miotech.kun.workflow.executor.AbstractQueueManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

@Singleton
public class KubernetesExecutor implements Executor {

    private final Logger logger = LoggerFactory.getLogger(KubernetesExecutor.class);
    private final WorkerLifeCycleManager podLifeCycleManager;//pod生命周期管理
    private final AbstractQueueManager kubernetesResourceManager;//管理kubernetes资源配额

    @Inject
    public KubernetesExecutor(PodLifeCycleManager podLifeCycleManager, KubernetesResourceManager kubernetesResourceManager) {
        this.podLifeCycleManager = podLifeCycleManager;
        this.kubernetesResourceManager = kubernetesResourceManager;
        init();
    }

    private void init() {
        Thread consumer = new Thread(new TaskAttemptConsumer(), "TaskAttemptConsumer");
        kubernetesResourceManager.init();
        consumer.start();
    }

    public boolean submit(TaskAttempt taskAttempt) {
        logger.info("submit taskAttemptId = {} to executor", taskAttempt.getId());
        kubernetesResourceManager.submit(taskAttempt);
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

    class TaskAttemptConsumer implements Runnable {
        @Override
        public void run() {
            while (true) {
                try {
                    TaskAttempt taskAttempt = kubernetesResourceManager.take();
                    if (taskAttempt == null) {
                        Uninterruptibles.sleepUninterruptibly(1, TimeUnit.SECONDS);
                        continue;
                    }
                    logger.debug("take taskAttempt = {} from queue = {}", taskAttempt.getId(), taskAttempt.getQueueName());
                    podLifeCycleManager.startWorker(taskAttempt);

                } catch (Throwable e) {
                    logger.error("failed to take taskAttempt from queue", e);
                }

            }
        }
    }
}
