package com.miotech.kun.workflow.executor.local;

import com.google.inject.Inject;
import com.miotech.kun.workflow.core.Executor;
import com.miotech.kun.workflow.core.model.resource.ResourceQueue;
import com.miotech.kun.workflow.core.model.task.TaskPriority;
import com.miotech.kun.workflow.core.model.taskrun.TaskAttempt;
import com.miotech.kun.workflow.executor.AbstractQueueManager;
import com.miotech.kun.workflow.executor.WorkerLifeCycleManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LocalExecutor implements Executor {

    private final Logger logger = LoggerFactory.getLogger(LocalExecutor.class);
    private final WorkerLifeCycleManager processLifeCycleManager;
    private final AbstractQueueManager localQueueManager;

    @Inject
    public LocalExecutor(WorkerLifeCycleManager processLifeCycleManager, LocalQueueManage localQueueManage) {
        this.processLifeCycleManager = processLifeCycleManager;
        this.localQueueManager = localQueueManage;
    }

    @Override
    public boolean reset() {
        logger.info("local executor going to reset");
        processLifeCycleManager.reset();
        return true;
    }

    @Override
    public boolean recover() {
        logger.info("local executor going to recover");
        processLifeCycleManager.recover();
        return true;
    }

    @Override
    public String workerLog(Long taskAttemptId, Integer tailLines) {
        return processLifeCycleManager.getLog(taskAttemptId, tailLines);
    }

    @Override
    public void changePriority(long taskAttemptId, String queueName, TaskPriority priority) {
        localQueueManager.changePriority(taskAttemptId, queueName, priority);
    }

    @Override
    public ResourceQueue createResourceQueue(ResourceQueue resourceQueue) {
        return localQueueManager.createResourceQueue(resourceQueue);
    }

    @Override
    public ResourceQueue updateResourceQueue(ResourceQueue resourceQueue) {
        return localQueueManager.updateResourceQueue(resourceQueue);
    }

    @Override
    public boolean submit(TaskAttempt taskAttempt) {
        logger.info("submit taskAttemptId = {} to executor", taskAttempt.getId());
        try {
            processLifeCycleManager.start(taskAttempt);
        }catch (Exception e){
            logger.error("failed to start taskAttempt = {}",taskAttempt.getId(),e);
            return false;
        }
        return true;
    }

    @Override
    public boolean cancel(Long taskAttemptId) {
        processLifeCycleManager.stop(taskAttemptId);
        return true;
    }
}
