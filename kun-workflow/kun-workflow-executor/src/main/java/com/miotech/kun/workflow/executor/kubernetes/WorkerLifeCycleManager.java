package com.miotech.kun.workflow.executor.kubernetes;

import com.miotech.kun.commons.utils.Props;
import com.miotech.kun.workflow.common.taskrun.dao.TaskRunDao;
import com.miotech.kun.workflow.core.model.taskrun.TaskAttempt;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunStatus;
import com.miotech.kun.workflow.core.model.worker.WorkerInstance;
import com.miotech.kun.workflow.core.model.worker.WorkerSnapshot;
import com.miotech.kun.workflow.executor.LifeCycleManager;
import com.miotech.kun.workflow.executor.WorkerEventHandler;
import com.miotech.kun.workflow.executor.WorkerMonitor;
import com.miotech.kun.workflow.executor.local.MiscService;
import com.miotech.kun.workflow.utils.DateTimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;


public abstract class WorkerLifeCycleManager implements LifeCycleManager {

    private final Logger logger = LoggerFactory.getLogger(WorkerLifeCycleManager.class);
    private final WorkerMonitor workerMonitor;
    protected final Props props;
    private final MiscService miscService;
    private final TaskRunDao taskRunDao;

    public WorkerLifeCycleManager(TaskRunDao taskRunDao,
                                  WorkerMonitor workerMonitor, Props props, MiscService miscService) {
        this.props = props;
        this.taskRunDao = taskRunDao;
        this.workerMonitor = workerMonitor;
        this.miscService = miscService;
    }

    /* ----------- public methods ------------ */

    public void recover() {
        List<WorkerInstance> instanceList = getRunningWorker();
        logger.info("recover watch pods size = {}", instanceList.size());
        for (WorkerInstance workerInstance : instanceList) {
            workerMonitor.register(workerInstance.getTaskAttemptId(), new PodEventHandler());
        }
    }

    public void reset() {
        workerMonitor.unRegisterAll();
    }


    @Override
    public WorkerInstance start(TaskAttempt taskAttempt) {
        WorkerSnapshot existWorkerSnapShot = get(taskAttempt.getId());
        if (existWorkerSnapShot != null) {
            throw new IllegalStateException("taskAttemptId = " + taskAttempt.getId() + " is running");
        }
        String logPath = logPathOfTaskAttempt(taskAttempt.getId());
        logger.debug("Update logPath to TaskAttempt. attemptId={}, path={}", taskAttempt.getId(), logPath);
        taskRunDao.updateTaskAttemptLogPath(taskAttempt.getId(), logPath);

        workerMonitor.register(taskAttempt.getId(), new PodEventHandler());
        WorkerSnapshot workerSnapshot = startWorker(taskAttempt
                .cloneBuilder()
                .withLogPath(logPath)
                .build());
        return workerSnapshot.getIns();
    }

    public String logPathOfTaskAttempt(Long taskAttemptId) {
        String date = DateTimeUtils.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return String.format("file:logs/%s/%s", date, taskAttemptId);
    }

    @Override
    public WorkerInstance stop(Long taskAttemptId) {
        logger.info("going to stop worker taskAttemptId = {}", taskAttemptId);
        WorkerSnapshot workerSnapshot = getWorker(taskAttemptId);
        if (workerSnapshot == null) {
            TaskAttempt taskAttempt = taskRunDao.fetchAttemptById(taskAttemptId).get();
            if (taskAttempt.getStatus().equals(TaskRunStatus.CREATED)) {
                abortTaskAttempt(taskAttemptId);
                return null;
            }
            if (taskAttempt.getStatus().isFinished()) {
                throw new IllegalStateException("unable to stop a finish worker");
            }
        }
        if (!stopWorker(taskAttemptId)) {
            throw new IllegalStateException("stop worker failed");
        }
        abortTaskAttempt(taskAttemptId);
        cleanupWorker(workerSnapshot.getIns());
        return workerSnapshot.getIns();
    }

    @Override
    public WorkerSnapshot get(Long taskAttemptId) {
        return getWorker(taskAttemptId);
    }



    /* ----------- abstract methods ------------ */

    public abstract WorkerSnapshot startWorker(TaskAttempt taskAttempt);

    public abstract Boolean stopWorker(Long taskAttemptId);

    public abstract WorkerSnapshot getWorker(Long taskAttemptId);

    public abstract String getWorkerLog(Long taskAttemptId, Integer tailLines);


    /* ----------- private methods ------------ */

    private void changeTaskRunStatus(WorkerSnapshot workerSnapshot) {
        TaskRunStatus taskRunStatus = workerSnapshot.getStatus();
        OffsetDateTime startAt = taskRunStatus.isRunning() ? workerSnapshot.getCreatedTime() : null;
        OffsetDateTime endAt = taskRunStatus.isFinished() ? workerSnapshot.getCreatedTime() : null;
        miscService.changeTaskAttemptStatus(workerSnapshot.getIns().getTaskAttemptId(),
                taskRunStatus, startAt, endAt);
    }

    private void abortTaskAttempt(Long taskAttemptId) {
        miscService.changeTaskAttemptStatus(taskAttemptId,
                TaskRunStatus.ABORTED, null, OffsetDateTime.now());
    }

    private void cleanupWorker(WorkerInstance workerInstance) {
        logger.info("unRegister worker,taskAttemptId = {}", workerInstance.getTaskAttemptId());
        workerMonitor.unRegister(workerInstance.getTaskAttemptId());
        stopWorker(workerInstance.getTaskAttemptId());

    }

    private boolean isFinish(WorkerSnapshot workerSnapshot) {
        return workerSnapshot.getStatus().isFinished();
    }

    class PodEventHandler implements WorkerEventHandler {
        private TaskRunStatus preStatus;

        public void onReceiveSnapshot(WorkerSnapshot workerSnapshot) {//处理pod状态变更
            logger.debug("receive worker snapshot:{}", workerSnapshot);
            if (preStatus == null || !preStatus.equals(workerSnapshot.getStatus())) {
                changeTaskRunStatus(workerSnapshot);
                preStatus = workerSnapshot.getStatus();
            }
            if (isFinish(workerSnapshot)) {
                logger.info("taskAttemptId = {},going to clean worker", workerSnapshot.getIns().getTaskAttemptId());
                cleanupWorker(workerSnapshot.getIns());
            }
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
