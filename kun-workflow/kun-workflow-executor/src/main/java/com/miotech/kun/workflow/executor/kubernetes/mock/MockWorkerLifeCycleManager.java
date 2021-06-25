package com.miotech.kun.workflow.executor.kubernetes.mock;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.miotech.kun.commons.utils.Props;
import com.miotech.kun.workflow.common.taskrun.dao.TaskRunDao;
import com.miotech.kun.workflow.core.model.taskrun.TaskAttempt;
import com.miotech.kun.workflow.core.model.worker.WorkerInstance;
import com.miotech.kun.workflow.core.model.worker.WorkerSnapshot;
import com.miotech.kun.workflow.executor.kubernetes.PodStatusSnapShot;
import com.miotech.kun.workflow.executor.kubernetes.WorkerLifeCycleManager;
import com.miotech.kun.workflow.executor.local.MiscService;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.PodSpec;
import io.fabric8.kubernetes.api.model.PodStatus;

import java.util.List;

@Singleton
public class MockWorkerLifeCycleManager extends WorkerLifeCycleManager {

    private MockQueueManager queueManager;
    private TaskRunDao taskRunDao;
    private MockWorkerMonitor workerMonitor;


    @Inject
    public MockWorkerLifeCycleManager(TaskRunDao taskRunDao, MockWorkerMonitor workerMonitor, Props props, MiscService miscService, MockQueueManager queueManager) {
        super(taskRunDao, workerMonitor, props, miscService, queueManager);
        this.queueManager = queueManager;
        this.taskRunDao = taskRunDao;
        this.workerMonitor = workerMonitor;
    }

    @Override
    public WorkerSnapshot startWorker(TaskAttempt taskAttempt) {
        WorkerInstance instance = WorkerInstance.newBuilder()
                .withTaskAttemptId(taskAttempt.getId()).build();
        queueManager.addWorker(taskAttempt);
        return new PodStatusSnapShot(instance, new PodStatus(), new PodSpec(), new ObjectMeta());
    }

    @Override
    public Boolean stopWorker(Long taskAttemptId) {
        queueManager.removeWorker(taskRunDao.fetchAttemptById(taskAttemptId).get());
        return true;
    }

    @Override
    public WorkerSnapshot getWorker(Long taskAttemptId) {
        if(workerMonitor.hasRegister(taskAttemptId)){
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
        return null;
    }

    public void markDone(Long taskAttemptId) {
        workerMonitor.makeDone(taskAttemptId);
    }

    public boolean hasRegister(Long taskAttemptId){
        return workerMonitor.hasRegister(taskAttemptId);
    }

}

