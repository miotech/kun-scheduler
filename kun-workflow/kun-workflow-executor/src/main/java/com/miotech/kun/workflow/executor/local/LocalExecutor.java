package com.miotech.kun.workflow.executor.local;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.miotech.kun.commons.utils.Props;
import com.miotech.kun.workflow.core.Executor;
import com.miotech.kun.workflow.core.model.WorkerLogs;
import com.miotech.kun.workflow.core.model.resource.ResourceQueue;
import com.miotech.kun.workflow.core.model.taskrun.TaskAttempt;
import com.miotech.kun.workflow.executor.storage.LocalStorageManager;
import com.miotech.kun.workflow.executor.storage.StorageManagerFactory;
import com.miotech.kun.workflow.executor.storage.StorageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

@Singleton
public class LocalExecutor implements Executor {

    private final Logger logger = LoggerFactory.getLogger(LocalExecutor.class);
    private final LocalProcessLifeCycleManager processLifeCycleManager;
    private final LocalQueueManage localQueueManager;
    private final LocalStorageManager localStorageManager;

    @Inject
    public LocalExecutor(LocalProcessLifeCycleManager processLifeCycleManager,
                         LocalQueueManage localQueueManage,
                         StorageManagerFactory storageManagerFactory,
                         Props props) {
        this.processLifeCycleManager = processLifeCycleManager;
        this.localQueueManager = localQueueManage;
        localStorageManager = (LocalStorageManager) storageManagerFactory.createStorageManager(StorageType.LOCAL);
        String storagePrefix = "executor.env.storage";
        Map<String, String> storageConfig = props.readValuesByPrefix(storagePrefix);
        localStorageManager.init(storageConfig);
        logger.info("local executor initialize");
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
    public WorkerLogs workerLog(Long taskAttemptId, Integer startLine, Integer endLine) {
        return localStorageManager.workerLog(taskAttemptId, startLine, endLine);
    }

    @Override
    public void uploadOperator(Long operatorId, String localFile) {
        //local executor not need to upload
    }

    @Override
    public void changePriority(long taskAttemptId, String queueName, Integer priority) {
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
        } catch (Exception e) {
            logger.error("failed to start taskAttempt = {}", taskAttempt.getId(), e);
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
