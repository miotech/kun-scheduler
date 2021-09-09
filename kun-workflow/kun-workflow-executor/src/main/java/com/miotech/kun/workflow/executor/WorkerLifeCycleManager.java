package com.miotech.kun.workflow.executor;

import com.google.common.util.concurrent.Uninterruptibles;
import com.miotech.kun.commons.utils.InitializingBean;
import com.miotech.kun.commons.utils.Props;
import com.miotech.kun.workflow.common.taskrun.dao.TaskRunDao;
import com.miotech.kun.workflow.core.model.taskrun.TaskAttempt;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunStatus;
import com.miotech.kun.workflow.core.model.worker.WorkerInstance;
import com.miotech.kun.workflow.core.model.worker.WorkerSnapshot;
import com.miotech.kun.workflow.executor.local.MiscService;
import com.miotech.kun.workflow.utils.DateTimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


public abstract class WorkerLifeCycleManager implements LifeCycleManager, InitializingBean {

    private final Logger logger = LoggerFactory.getLogger(WorkerLifeCycleManager.class);
    protected final WorkerMonitor workerMonitor;
    protected final Props props;
    private final MiscService miscService;
    protected final TaskRunDao taskRunDao;
    protected final AbstractQueueManager queueManager;
    private Thread consumer = new Thread(new TaskAttemptConsumer(), "TaskAttemptConsumer");

    public WorkerLifeCycleManager(TaskRunDao taskRunDao, WorkerMonitor workerMonitor,
                                  Props props, MiscService miscService, AbstractQueueManager queueManager) {
        this.props = props;
        this.taskRunDao = taskRunDao;
        this.workerMonitor = workerMonitor;
        this.miscService = miscService;
        this.queueManager = queueManager;
    }


    /* ----------- public methods ------------ */
    @Override
    public void afterPropertiesSet(){
        init();
    }

    public void init() {
        queueManager.init();
        consumer.start();
    }

    public void shutdown() {
        queueManager.reset();
        consumer.interrupt();
        workerMonitor.unRegisterAll();
    }

    public void reset() {
        workerMonitor.unRegisterAll();
        queueManager.reset();
    }

    public void recover() {
        List<TaskAttempt> shouldRecoverAttemptList = taskRunDao.fetchTaskAttemptListForRecover(Arrays.asList(TaskRunStatus.QUEUED, TaskRunStatus.ERROR, TaskRunStatus.RUNNING));
        List<WorkerInstance> instanceList = getRunningWorker();
        logger.info("recover watch pods size = {}", instanceList.size());
        for (WorkerInstance workerInstance : instanceList) {
            workerMonitor.register(workerInstance.getTaskAttemptId(), new InnerEventHandler());
        }
        Set<Long> instanceSet = instanceList.stream().map(WorkerInstance::getTaskAttemptId).collect(Collectors.toSet());
        //filter taskAttempt which instance still running
        List<TaskAttempt> recoverAttemptList = shouldRecoverAttemptList.stream().filter(x -> !instanceSet.contains(x.getId())).collect(Collectors.toList());
        logger.info("recover queued attempt size = {}", recoverAttemptList.size());
        for (TaskAttempt taskAttempt : recoverAttemptList) {
            queueManager.submit(taskAttempt);
        }
    }


    @Override
    public void start(TaskAttempt taskAttempt) {
        WorkerSnapshot existWorkerSnapShot = get(taskAttempt.getId());
        if (existWorkerSnapShot != null) {
            throw new IllegalStateException("taskAttemptId = " + taskAttempt.getId() + " is running");
        }
        queueManager.submit(taskAttempt);
    }

    @Override
    public String getLog(Long taskAttemptId, Integer tailLines){
        return getWorkerLog(taskAttemptId,tailLines);
    }

    public String logPathOfTaskAttempt(Long taskAttemptId) {
        String date = DateTimeUtils.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return String.format("file:logs/%s/%s", date, taskAttemptId);
    }

    @Override
    public void stop(Long taskAttemptId) {
        logger.info("going to stop worker taskAttemptId = {}", taskAttemptId);
        WorkerSnapshot workerSnapshot = getWorker(taskAttemptId);
        if (workerSnapshot == null) {
            TaskAttempt taskAttempt = taskRunDao.fetchAttemptById(taskAttemptId).get();
            if (taskAttempt.getStatus().equals(TaskRunStatus.CREATED)) {
                updateTaskAttemptAborted(taskAttemptId);
                return;
            }
            if (taskAttempt.getStatus().equals(TaskRunStatus.QUEUED)) {
                queueManager.remove(taskAttempt);
                updateTaskAttemptAborted(taskAttemptId);
                return;
            }
            if (taskAttempt.getStatus().isFinished()) {
                throw new IllegalStateException("unable to stop a finish worker");
            }
        }
        if (!stopWorker(taskAttemptId)) {
            throw new IllegalStateException("stop worker failed");
        }
        updateTaskAttemptAborted(taskAttemptId);
        cleanupWorker(workerSnapshot.getIns());
    }

    @Override
    public WorkerSnapshot get(Long taskAttemptId) {
        return getWorker(taskAttemptId);
    }



    /* ----------- abstract methods ------------ */

    protected abstract void startWorker(TaskAttempt taskAttempt);

    protected abstract Boolean stopWorker(Long taskAttemptId);

    protected abstract WorkerSnapshot getWorker(Long taskAttemptId);

    protected abstract String getWorkerLog(Long taskAttemptId, Integer tailLines);


    /* ----------- private methods ------------ */


    private void executeTaskAttempt(TaskAttempt taskAttempt) {
        WorkerSnapshot existWorkerSnapShot = get(taskAttempt.getId());
        if (existWorkerSnapShot != null) {
            logger.warn("taskAttemptId = {} is running", taskAttempt.getId());
            return;
        }
        String logPath = logPathOfTaskAttempt(taskAttempt.getId());
        logger.debug("Update logPath to TaskAttempt. attemptId={}, path={}", taskAttempt.getId(), logPath);
        taskRunDao.updateTaskAttemptLogPath(taskAttempt.getId(), logPath);

        workerMonitor.register(taskAttempt.getId(), new InnerEventHandler());
        try {
            startWorker(taskAttempt
                    .cloneBuilder()
                    .withLogPath(logPath)
                    .build());
        } catch (Exception e) {
            logger.warn("Failed to execute worker, taskAttemptId = {}", taskAttempt.getId(), e);
            miscService.changeTaskAttemptStatus(taskAttempt.getId(), TaskRunStatus.ERROR);
        }
    }

    private void changeTaskRunStatus(WorkerSnapshot workerSnapshot) {
        TaskRunStatus taskRunStatus = workerSnapshot.getStatus();
        OffsetDateTime startAt = taskRunStatus.isRunning() ? workerSnapshot.getCreatedTime() : null;
        OffsetDateTime endAt = taskRunStatus.isFinished() ? workerSnapshot.getCreatedTime() : null;
        miscService.changeTaskAttemptStatus(workerSnapshot.getIns().getTaskAttemptId(),
                taskRunStatus, startAt, endAt);
    }

    private void updateTaskAttemptAborted(Long taskAttemptId) {
        miscService.changeTaskAttemptStatus(taskAttemptId,
                TaskRunStatus.ABORTED, null, DateTimeUtils.now());
    }

    private void cleanupWorker(WorkerInstance workerInstance) {
        workerMonitor.unRegister(workerInstance.getTaskAttemptId());
        stopWorker(workerInstance.getTaskAttemptId());

    }

    private boolean retryTaskAttemptIfNecessary(WorkerInstance workerInstance) {
        TaskAttempt taskAttempt = taskRunDao.fetchAttemptById(workerInstance.getTaskAttemptId()).get();
        Integer retryLimit = taskAttempt.getTaskRun().getTask().getRetries();
        if (taskAttempt.getRetryTimes() >= retryLimit) {
            logger.debug("taskAttempt = {} has reach retry limit,retry limit = {}",taskAttempt.getId(),retryLimit);
            return false;
        }
        logger.debug("taskAttempt = {} going to  retry,next retry times = {}",taskAttempt.getId(),taskAttempt.getRetryTimes() + 1);
        TaskAttempt retryTaskAttempt = taskAttempt.cloneBuilder()
                .withRetryTimes(taskAttempt.getRetryTimes() + 1)
                .build();
        taskRunDao.updateAttempt(retryTaskAttempt);
        cleanupWorker(workerInstance);
        Integer retryDelay = taskAttempt.getTaskRun().getTask().getRetryDelay();
        //sleep to delay
        if(retryDelay != null && retryDelay > 0){
            logger.debug("taskAttempt = {} wait {}s for  retry",taskAttempt.getId(),retryDelay);
            Uninterruptibles.sleepUninterruptibly(retryDelay, TimeUnit.SECONDS);
        }
        //retry attempt
        queueManager.submit(taskAttempt);
        return true;
    }

    private boolean isFinish(WorkerSnapshot workerSnapshot) {
        return workerSnapshot.getStatus().isFinished();
    }

    private boolean isFailed(WorkerSnapshot workerSnapshot) {
        return workerSnapshot.getStatus().equals(TaskRunStatus.FAILED);
    }

    private class InnerEventHandler implements WorkerEventHandler {
        private TaskRunStatus preStatus;

        public void onReceiveSnapshot(WorkerSnapshot workerSnapshot) {//处理pod状态变更
            logger.debug("receive worker snapshot:{}", workerSnapshot);
            if (isFailed(workerSnapshot)) {
                if (retryTaskAttemptIfNecessary(workerSnapshot.getIns())) {
                    return;
                }
            }
            if (preStatus == null || !preStatus.equals(workerSnapshot.getStatus())) {
                changeTaskRunStatus(workerSnapshot);
                preStatus = workerSnapshot.getStatus();
            }
            if (isFinish(workerSnapshot)) {
                logger.info("taskAttemptId = {},going to clean worker", workerSnapshot.getIns().getTaskAttemptId());
                try {
                    miscService.notifyFinished(workerSnapshot.getIns().getTaskAttemptId(), workerSnapshot.getStatus());
                } catch (Exception e) {
                    logger.warn("notify finished event with taskAttemptId = {} failed", workerSnapshot.getIns().getTaskAttemptId(), e);
                }
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

    class TaskAttemptConsumer implements Runnable {
        @Override
        public void run() {
            while (true) {
                if(Thread.currentThread().isInterrupted()){
                    break;
                }
                try {
                    List<TaskAttempt> readyToExecuteTaskAttemptList = queueManager.drain();
                    if (readyToExecuteTaskAttemptList.size() == 0) {
                        Uninterruptibles.sleepUninterruptibly(1, TimeUnit.SECONDS);
                        continue;
                    }
                    for (TaskAttempt taskAttempt : readyToExecuteTaskAttemptList) {
                        try {
                            logger.debug("take taskAttempt = {} from queue = {}", taskAttempt.getId(), taskAttempt.getQueueName());
                            executeTaskAttempt(taskAttempt);
                        } catch (Exception e) {
                            logger.warn("take taskAttempt = {} failed", taskAttempt.getId(), e);
                        }
                    }
                } catch ( Throwable e) {
                    logger.error("failed to take taskAttempt from queue", e);
                }

            }
        }
    }

}
