package com.miotech.kun.workflow.executor.kubernetes;

import com.google.inject.Inject;
import com.miotech.kun.commons.utils.Props;
import com.miotech.kun.workflow.common.taskrun.dao.TaskRunDao;
import com.miotech.kun.workflow.common.workerInstance.WorkerInstanceDao;
import com.miotech.kun.workflow.core.model.taskrun.TaskAttempt;
import com.miotech.kun.workflow.core.model.worker.WorkerSnapshot;
import com.miotech.kun.workflow.executor.WorkerMonitor;
import com.miotech.kun.workflow.executor.local.MiscService;

import java.util.List;

public class PodLifeCycleManager extends WorkerLifeCycleManager{


    @Inject
    public PodLifeCycleManager(TaskRunDao taskRunDao, WorkerInstanceDao workerInstanceDao,
                               WorkerMonitor workerMonitor, Props props, MiscService miscService) {
        super(taskRunDao, workerInstanceDao, workerMonitor, props, miscService);
    }

    @Override
    public WorkerSnapshot startWorker(TaskAttempt taskAttempt) {
        return null;
    }

    @Override
    public WorkerSnapshot stopWorker(WorkerSnapshot workerSnapshot) {
        return null;
    }

    @Override
    public WorkerSnapshot getWorker(TaskAttempt taskAttempt) {
        return null;
    }

    @Override
    public void checkPollingWorker(List<WorkerSnapshot> workerSnapshots) {

    }
}
