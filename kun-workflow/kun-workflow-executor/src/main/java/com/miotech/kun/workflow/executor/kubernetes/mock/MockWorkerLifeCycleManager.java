package com.miotech.kun.workflow.executor.kubernetes.mock;

import com.google.inject.Injector;
import com.miotech.kun.workflow.core.model.taskrun.TaskAttempt;
import com.miotech.kun.workflow.core.model.worker.WorkerInstance;
import com.miotech.kun.workflow.core.model.worker.WorkerSnapshot;
import com.miotech.kun.workflow.executor.WorkerLifeCycleManager;
import com.miotech.kun.workflow.executor.config.ExecutorConfig;
import com.miotech.kun.workflow.executor.kubernetes.PodStatusSnapShot;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.PodSpec;
import io.fabric8.kubernetes.api.model.PodStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class MockWorkerLifeCycleManager extends WorkerLifeCycleManager {

    private final Logger logger = LoggerFactory.getLogger(WorkerLifeCycleManager.class);

    private MockQueueManager mockQueueManager;
    private MockWorkerMonitor mockWorkerMonitor;


    public MockWorkerLifeCycleManager(ExecutorConfig executorConfig, MockWorkerMonitor workerMonitor,
                                      MockQueueManager queueManager) {
        super(executorConfig, workerMonitor, queueManager, "test");
        this.mockQueueManager = queueManager;
        this.mockWorkerMonitor = workerMonitor;
    }

    @Override
    public void injectMembers(Injector injector) {
        injector.injectMembers(this);
    }

    @Override
    public void startWorker(TaskAttempt taskAttempt) {
        logger.info("start worker taskAttemptId = {}", taskAttempt.getId());
        WorkerInstance instance = WorkerInstance.newBuilder()
                .withTaskAttemptId(taskAttempt.getId()).build();
        mockQueueManager.addWorker(taskAttempt);
    }

    @Override
    public Boolean stopWorker(Long taskAttemptId) {
        mockQueueManager.removeWorker(taskRunDao.fetchAttemptById(taskAttemptId).get());
        return true;
    }

    @Override
    public WorkerSnapshot getWorker(Long taskAttemptId) {
        if (mockWorkerMonitor.hasRegister(taskAttemptId)) {
            WorkerInstance instance = WorkerInstance.newBuilder()
                    .withTaskAttemptId(taskAttemptId).build();
            return new PodStatusSnapShot(instance, new PodStatus(), new PodSpec(), new ObjectMeta());
        }
        return null;
    }

    @Override
    public String getWorkerLog(Long taskAttemptId, Integer tailLines) {
        return null;
    }

    @Override
    public List<WorkerInstance> getRunningWorker() {
        return mockWorkerMonitor.allRegister();
    }

    public void markDone(Long taskAttemptId) {
        mockWorkerMonitor.makeDone(taskAttemptId);
    }

    public void markFailed(Long taskAttemptId) {
        mockWorkerMonitor.makeFailed(taskAttemptId);
    }

    public void markRunning(Long taskAttemptId) {
        mockWorkerMonitor.makeRunning(taskAttemptId);
    }

    public boolean hasRegister(Long taskAttemptId) {
        return mockWorkerMonitor.hasRegister(taskAttemptId);
    }

}

