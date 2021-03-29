package com.miotech.kun.workflow.executor.kubernetes;

import com.miotech.kun.commons.utils.Props;
import com.miotech.kun.workflow.common.taskrun.dao.TaskRunDao;
import com.miotech.kun.workflow.common.workerInstance.WorkerInstanceDao;
import com.miotech.kun.workflow.core.model.taskrun.TaskAttempt;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunStatus;
import com.miotech.kun.workflow.core.model.worker.WorkerInstance;
import com.miotech.kun.workflow.core.model.worker.WorkerInstanceEnv;
import com.miotech.kun.workflow.core.model.worker.WorkerSnapshot;
import com.miotech.kun.workflow.executor.LifeCycleManager;
import com.miotech.kun.workflow.executor.WorkerEventHandler;
import com.miotech.kun.workflow.executor.WorkerMonitor;
import com.miotech.kun.workflow.executor.local.MiscService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;


public abstract class WorkerLifeCycleManager implements LifeCycleManager {

    private final Logger logger = LoggerFactory.getLogger(WorkerLifeCycleManager.class);
    private final TaskRunDao taskRunDao;
    private final WorkerInstanceDao workerInstanceDao;
    private final WorkerMonitor workerMonitor;
    private final Props props;
    private final WorkerInstanceEnv env;
    private final MiscService miscService;

    public WorkerLifeCycleManager(TaskRunDao taskRunDao, WorkerInstanceDao workerInstanceDao,
                                  WorkerMonitor workerMonitor, Props props, MiscService miscService) {
        this.taskRunDao = taskRunDao;
        this.props = props;
        this.env = WorkerInstanceEnv.valueOf(props.getString("executor.env"));
        this.workerInstanceDao = workerInstanceDao;
        this.workerMonitor = workerMonitor;
        this.miscService = miscService;
    }

    /* ----------- public methods ------------ */
    //todo:init after construct
    public void init() {
        List<WorkerInstance> instanceList = getRunningWorker();
        for (WorkerInstance workerInstance : instanceList) {
            workerMonitor.register(workerInstance, new PodEventHandler());
        }
    }

    public List<WorkerInstance> getRunningWorker() {
        return workerInstanceDao.getActiveWorkerInstance(env);
    }

    @Override
    public WorkerInstance start(TaskAttempt taskAttempt) {
        WorkerSnapshot workerSnapshot = startWorker(taskAttempt);
        changeTaskRunStatus(workerSnapshot);
        workerMonitor.register(workerSnapshot.getIns(), new PodEventHandler());
        return workerSnapshot.getIns();
    }

    @Override
    public WorkerInstance stop(TaskAttempt taskAttempt) {
        WorkerSnapshot workerSnapshot = getWorker(taskAttempt);
        if (isFinish(workerSnapshot)) {
            throw new IllegalStateException("unable to stop a finish worker");
        }
        WorkerSnapshot stoppedSnapshot = stopWorker(workerSnapshot);
        changeTaskRunStatus(stoppedSnapshot);
        cleanupWorker(stoppedSnapshot.getIns());
        return stoppedSnapshot.getIns();
    }

    @Override
    public WorkerSnapshot get(TaskAttempt taskAttempt) {
        return getWorker(taskAttempt);
    }


    /* ----------- abstract methods ------------ */

    public abstract WorkerSnapshot startWorker(TaskAttempt taskAttempt);

    public abstract WorkerSnapshot stopWorker(WorkerSnapshot workerSnapshot);

    public abstract WorkerSnapshot getWorker(TaskAttempt taskAttempt);

    public abstract void checkPollingWorker(List<WorkerSnapshot> workerSnapshots);


    /* ----------- private methods ------------ */

    private void changeTaskRunStatus(WorkerSnapshot workerSnapshot) {
        TaskRunStatus taskRunStatus = workerSnapshot.getStatus();
        miscService.changeTaskAttemptStatus(workerSnapshot.getIns().getTaskAttemptId(),
                taskRunStatus, null, workerSnapshot.getCreatedTime());
    }

    private void cleanupWorker(WorkerInstance workerInstance) {
        workerMonitor.unRegister(workerInstance.getTaskAttemptId());
        workerInstanceDao.deleteWorkerInstance(workerInstance.getTaskAttemptId(), env);

    }

    private boolean isFinish(WorkerSnapshot workerSnapshot) {
        return workerSnapshot.getStatus().isFinished();
    }

    class PodEventHandler implements WorkerEventHandler {
        private TaskRunStatus preStatus;

        public void onReceiveSnapshot(WorkerSnapshot workerSnapshot) {//处理pod状态变更
            if (isFinish(workerSnapshot)) {
                cleanupWorker(workerSnapshot.getIns());
            }
            changeTaskRunStatus(workerSnapshot);
            preStatus = workerSnapshot.getStatus();
        }

        @Override
        public void onReceivePollingSnapShot(WorkerSnapshot workerSnapshot) {
            if (workerSnapshot.getStatus().equals(preStatus)) {
                return;
            }
            onReceiveSnapshot(workerSnapshot);
        }

    }

}
