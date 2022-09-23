package com.miotech.kun.workflow.executor.local;

import com.google.inject.Injector;
import com.miotech.kun.workflow.core.Executor;
import com.miotech.kun.workflow.core.model.WorkerLogs;
import com.miotech.kun.workflow.core.model.executor.ExecutorInfo;
import com.miotech.kun.workflow.core.model.resource.ResourceQueue;
import com.miotech.kun.workflow.core.model.taskrun.TaskAttempt;
import com.miotech.kun.workflow.executor.config.ExecutorConfig;
import com.miotech.kun.workflow.executor.storage.LocalStorageManager;
import com.miotech.kun.workflow.executor.storage.StorageManagerFactory;
import com.miotech.kun.workflow.executor.storage.StorageType;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Map;

public class LocalExecutor implements Executor {

    private final Logger logger = LoggerFactory.getLogger(LocalExecutor.class);
    private LocalProcessLifeCycleManager processLifeCycleManager;
    private LocalQueueManage localQueueManager;
    private LocalStorageManager localStorageManager;
    private final ExecutorConfig executorConfig;


    public LocalExecutor(ExecutorConfig executorConfig) {
        this.executorConfig = executorConfig;
        LocalProcessBackend localProcessBackend = new LocalProcessBackend();
        LocalProcessMonitor localProcessMonitor = new LocalProcessMonitor(localProcessBackend);
        this.localQueueManager = new LocalQueueManage(executorConfig,localProcessBackend);
        this.processLifeCycleManager = new LocalProcessLifeCycleManager(executorConfig,localProcessMonitor,localQueueManager,localProcessBackend);
        localStorageManager = (LocalStorageManager) StorageManagerFactory.createStorageManager(StorageType.LOCAL);

        logger.info("local executor initialize");
    }

    @Override
    public void shutdown() {
        processLifeCycleManager.shutdown();
    }

    @Override
    public void injectMembers(Injector injector) {
        localStorageManager.injectMember(injector);
        localQueueManager.injectMember(injector);
        processLifeCycleManager.injectMembers(injector);
        injector.injectMembers(this);
    }

    @Override
    public void init() {
        processLifeCycleManager.init();
        Map<String, String> storageConfig = executorConfig.getStorage();
        localStorageManager.init(storageConfig);
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
    public boolean getMaintenanceMode() {
        return processLifeCycleManager.getMaintenanceMode();
    }

    @Override
    public void setMaintenanceMode(boolean mode) {
        processLifeCycleManager.setMaintenanceMode(mode);
    }

    @Override
    public ExecutorInfo getExecutorInfo() {
        return ExecutorInfo.newBuilder()
                .withKind(executorConfig.getKind())
                .withName(executorConfig.getName())
                .withLabels(Arrays.asList(StringUtils.split(executorConfig.getLabel(), ",")))
                .withResourceQueues(executorConfig.getResourceQueues())
                .build();
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
    public void execute(TaskAttempt taskAttempt) {
        processLifeCycleManager.executeTaskAttempt(taskAttempt);
    }

    @Override
    public void check(TaskAttempt taskAttempt) {
        processLifeCycleManager.check(taskAttempt);
    }

    @Override
    public boolean cancel(Long taskAttemptId) {
        processLifeCycleManager.stop(taskAttemptId);
        return true;
    }
}
