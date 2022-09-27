package com.miotech.kun.workflow.executor;

import com.google.common.collect.Lists;
import com.google.common.eventbus.EventBus;
import com.google.common.util.concurrent.Uninterruptibles;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.miotech.kun.commons.pubsub.subscribe.EventSubscriber;
import com.miotech.kun.commons.utils.Props;
import com.miotech.kun.workflow.common.taskrun.bo.TaskAttemptProps;
import com.miotech.kun.workflow.common.taskrun.dao.TaskRunDao;
import com.miotech.kun.workflow.core.event.CheckResultEvent;
import com.miotech.kun.workflow.core.event.TaskRunTransitionEvent;
import com.miotech.kun.workflow.core.event.TaskRunTransitionEventType;
import com.miotech.kun.workflow.core.model.task.CheckType;
import com.miotech.kun.workflow.core.model.taskrun.TaskAttempt;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunPhase;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunStatus;
import com.miotech.kun.workflow.core.model.worker.WorkerInstance;
import com.miotech.kun.workflow.core.model.worker.WorkerSnapshot;
import com.miotech.kun.workflow.executor.config.ExecutorConfig;
import com.miotech.kun.workflow.utils.DateTimeUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;


public abstract class WorkerLifeCycleManager implements LifeCycleManager {

    private final Logger logger = LoggerFactory.getLogger(WorkerLifeCycleManager.class);
    @Inject
    protected TaskRunDao taskRunDao;
    @Inject
    private EventBus eventBus;
    @Inject
    private EventSubscriber eventSubscriber;
    protected WorkerMonitor workerMonitor;
    @Inject
    protected Props props;
    protected AbstractQueueManager queueManager;
    private Thread consumer = new Thread(new TaskAttemptConsumer(), "TaskAttemptConsumer");
    protected final String name;
    protected final ExecutorConfig executorConfig;
    private boolean maintenanceMode;

    public WorkerLifeCycleManager(ExecutorConfig executorConfig, WorkerMonitor workerMonitor,
                                  AbstractQueueManager queueManager, String name ) {
        this.workerMonitor = workerMonitor;
        this.queueManager = queueManager;
        this.name = name;
        this.executorConfig = executorConfig;
        this.maintenanceMode = false;
        logger.info("{} worker life cycle manager initialize", name);
    }


    /* ----------- public methods ------------ */

    public void init() {

        queueManager.init();
        consumer.start();
        workerMonitor.start();
        eventSubscriber.subscribe(event -> {
            if (event instanceof CheckResultEvent) {
                logger.debug("receive check result event = {}", event);
                handleDataQualityEvent((CheckResultEvent) event);
            }
        });
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
    public String getLog(Long taskAttemptId, Integer tailLines) {
        return getWorkerLog(taskAttemptId, tailLines);
    }


    @Override
    public void stop(Long taskAttemptId) {
        logger.info("going to stop worker taskAttemptId = {}", taskAttemptId);
        WorkerSnapshot workerSnapshot = getWorker(taskAttemptId);
        if (workerSnapshot == null) {
            TaskAttempt taskAttempt = taskRunDao.fetchAttemptById(taskAttemptId).get();
            if (TaskRunPhase.isCreated(taskAttempt.getTaskRunPhase()) || TaskRunPhase.isWaiting(taskAttempt.getTaskRunPhase())) {
                return;
            } else if (TaskRunPhase.isQueued(taskAttempt.getTaskRunPhase())) {
                queueManager.remove(taskAttempt);
                return;
            } else {
                throw new IllegalStateException("unable to stop taskRun with status: " + taskAttempt.getStatus());
            }
        }
        if (!stopWorker(taskAttemptId)) {
            throw new IllegalStateException("stop worker failed");
        }
        cleanupWorker(workerSnapshot.getIns());
    }

    @Override
    public WorkerSnapshot get(Long taskAttemptId) {
        return getWorker(taskAttemptId);
    }

    public boolean getMaintenanceMode() {
        return maintenanceMode;
    }

    public void setMaintenanceMode(boolean mode) {
        maintenanceMode = mode;
    }


    /* ----------- abstract methods ------------ */

    protected abstract void injectMembers(Injector injector);

    protected abstract void startWorker(TaskAttempt taskAttempt);

    protected abstract Boolean stopWorker(Long taskAttemptId);

    protected abstract WorkerSnapshot getWorker(Long taskAttemptId);

    protected abstract String getWorkerLog(Long taskAttemptId, Integer tailLines);

    protected String logPathOfTaskAttempt(Long taskAttemptId) {
        String date = DateTimeUtils.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return String.format("file:logs/%s/%s", date, taskAttemptId);
    }


    /* ----------- private methods ------------ */


    public void executeTaskAttempt(TaskAttempt taskAttempt) {
        WorkerSnapshot existWorkerSnapShot = get(taskAttempt.getId());
        if (existWorkerSnapShot != null) {
            logger.warn("taskAttemptId = {} is running , re-register monitor", taskAttempt.getId());
            workerMonitor.register(taskAttempt.getId(), new InnerEventHandler());
            return;
        }
        String logPath = logPathOfTaskAttempt(taskAttempt.getId());
        logger.debug("Update logPath to TaskAttempt. attemptId={}, path={}", taskAttempt.getId(), logPath);
        taskRunDao.updateTaskAttemptLogPath(taskAttempt.getId(), logPath);
        Long taskAttemptId = taskAttempt.getId();
        workerMonitor.register(taskAttemptId, new InnerEventHandler());
        try {
            startWorker(taskAttempt
                    .cloneBuilder()
                    .withLogPath(logPath)
                    .build());
        } catch (Exception e) {
            logger.warn("Failed to execute worker, taskAttemptId = {}", taskAttemptId, e);
            sendExceptionEvent(taskAttemptId);
        }
    }


    private void sendExceptionEvent(Long taskAttemptId) {
        TaskRunTransitionEvent taskRunTransitionEvent = new TaskRunTransitionEvent(TaskRunTransitionEventType.EXCEPTION, taskAttemptId , null);
        eventBus.post(taskRunTransitionEvent);
    }

    private void sendRunningEvent(Long taskAttemptId) {
        TaskRunTransitionEvent taskRunTransitionEvent = new TaskRunTransitionEvent(TaskRunTransitionEventType.READY, taskAttemptId , null);
        eventBus.post(taskRunTransitionEvent);
    }

    private void sendCheckEvent(Long taskAttemptId) {
        TaskRunTransitionEvent taskRunTransitionEvent = new TaskRunTransitionEvent(TaskRunTransitionEventType.COMPLETE, taskAttemptId, null);
        eventBus.post(taskRunTransitionEvent);
    }

    private void sendFailedEvent(Long taskAttemptId) {
        TaskRunTransitionEvent taskRunTransitionEvent = new TaskRunTransitionEvent(TaskRunTransitionEventType.FAILED, taskAttemptId, null);
        eventBus.post(taskRunTransitionEvent);
    }

    private void sendCheckFailedEvent(Long taskAttemptId) {
        TaskRunTransitionEvent taskRunTransitionEvent = new TaskRunTransitionEvent(TaskRunTransitionEventType.CHECK_FAILED, taskAttemptId, null);
        eventBus.post(taskRunTransitionEvent);
    }

    private void sendCheckSuccessEvent(Long taskAttemptId) {
        TaskRunTransitionEvent taskRunTransitionEvent = new TaskRunTransitionEvent(TaskRunTransitionEventType.CHECK_SUCCESS, taskAttemptId, null);
        eventBus.post(taskRunTransitionEvent);
    }

    private void sendReSubmitEvent(Long taskAttemptId){
        TaskRunTransitionEvent taskRunTransitionEvent = new TaskRunTransitionEvent(TaskRunTransitionEventType.RESUBMIT, taskAttemptId, null);
        eventBus.post(taskRunTransitionEvent);
    }

    public void check(TaskAttempt taskAttempt) {
        logger.debug("taskAttempt = {} going to check", taskAttempt.getId());
        CheckType checkType = taskAttempt.getTaskRun().getTask().getCheckType();
        logger.debug("taskAttemptId = {},checkType is {}", taskAttempt.getId(), checkType.name());
        switch (checkType) {
            case SKIP:
                sendCheckSuccessEvent(taskAttempt.getId());
                break;
            case WAIT_EVENT:
            case WAIT_EVENT_PASS:
                break;
            default:
                throw new IllegalArgumentException("illegal check type");
        }
    }

    private void cleanupWorker(WorkerInstance workerInstance) {
        workerMonitor.unRegister(workerInstance.getTaskAttemptId());
        stopWorker(workerInstance.getTaskAttemptId());

    }

    private boolean retryTaskAttemptIfNecessary(WorkerInstance workerInstance) {
        TaskAttempt taskAttempt = taskRunDao.fetchAttemptById(workerInstance.getTaskAttemptId()).get();
        Integer retryLimit = taskAttempt.getTaskRun().getTask().getRetries();
        if (taskAttempt.getRetryTimes() >= retryLimit) {
            logger.debug("taskAttempt = {} has reach retry limit,retry limit = {}", taskAttempt.getId(), retryLimit);
            return false;
        }
        logger.debug("taskAttempt = {} going to  retry,next retry time = {}", taskAttempt.getId(), taskAttempt.getRetryTimes() + 1);
        Integer retryDelay = taskAttempt.getTaskRun().getTask().getRetryDelay();
        //sleep to delay
        if (retryDelay != null && retryDelay > 0) {
            logger.debug("taskAttempt = {} wait {}s for  retry", taskAttempt.getId(), retryDelay);
            Uninterruptibles.sleepUninterruptibly(retryDelay, TimeUnit.SECONDS);
        }

        TaskAttempt retryTaskAttempt = taskAttempt.cloneBuilder()
                .withRetryTimes(taskAttempt.getRetryTimes() + 1)
                .build();
        taskRunDao.updateAttempt(retryTaskAttempt);

        sendReSubmitEvent(taskAttempt.getId());
        return true;
    }

    private boolean isRunning(WorkerSnapshot workerSnapshot) {
        return workerSnapshot.getStatus().isRunning();
    }

    private boolean isFailed(WorkerSnapshot workerSnapshot) {
        return workerSnapshot.getStatus().equals(TaskRunStatus.FAILED);
    }

    private boolean isSuccess(WorkerSnapshot workerSnapshot) {
        return workerSnapshot.getStatus().isSuccess();
    }

    private class InnerEventHandler implements WorkerEventHandler {
        private TaskRunStatus preStatus;

        public void onReceiveSnapshot(WorkerSnapshot workerSnapshot) {//处理pod状态变更
            logger.debug("receive worker snapshot:{}", workerSnapshot);
            if (preStatus != null && preStatus.equals(workerSnapshot.getStatus())) {
                //ignore duplicate event
                return;
            }
            preStatus = workerSnapshot.getStatus();
            Long taskAttemptId = workerSnapshot.getIns().getTaskAttemptId();
            if (isRunning(workerSnapshot)) {
                //ignore running event
//                sendRunningEvent(taskAttemptId);
                return;
            }
            // worker is not running ,clean up
            cleanupWorker(workerSnapshot.getIns());

            if (isFailed(workerSnapshot)) {
                if (!retryTaskAttemptIfNecessary(workerSnapshot.getIns())) {
                    sendFailedEvent(taskAttemptId);
                }
            } else if (isSuccess(workerSnapshot)) {
                sendCheckEvent(taskAttemptId);
            } else {
                throw new IllegalStateException("illegal worker status");
            }
        }

        @Override
        public void onReceivePollingSnapShot(WorkerSnapshot workerSnapshot) {
            if (preStatus != null && workerSnapshot.getStatus().equals(preStatus)) {
                return;
            }
            onReceiveSnapshot(workerSnapshot);
        }


    }

    private void handleDataQualityEvent(CheckResultEvent event) {
        Long taskRunId = event.getTaskRunId();
        TaskAttemptProps taskAttemptProps = taskRunDao.fetchLatestTaskAttempt(taskRunId);
        if (taskAttemptProps.getStatus().isFinished()) {
            logger.debug("taskAttempt = {} is finished,ignore event = {}", taskAttemptProps.getId(), event);
            return;
        }
        if (event.getCheckStatus()) {
            sendCheckSuccessEvent(taskAttemptProps.getId());
        } else {
            sendCheckFailedEvent(taskAttemptProps.getId());
        }
    }

    class TaskAttemptConsumer implements Runnable {
        @Override
        public void run() {
            logger.info("{} life cycle manager - task attempt consumer start to run... ", name);
            while (!Thread.currentThread().isInterrupted()) {

                if (maintenanceMode) {
                    Uninterruptibles.sleepUninterruptibly(1, TimeUnit.SECONDS);
                    continue;
                }

                try {
                    List<TaskAttempt> readyToExecuteTaskAttemptList = queueManager.drain();
                    if (readyToExecuteTaskAttemptList.size() == 0) {
                        Uninterruptibles.sleepUninterruptibly(1, TimeUnit.SECONDS);
                        continue;
                    }
                    for (TaskAttempt taskAttempt : readyToExecuteTaskAttemptList) {
                        try {
                            logger.debug("{} take taskAttempt = {} from queue = {}", name, taskAttempt.getId(), taskAttempt.getQueueName());
                            eventBus.post(new TaskRunTransitionEvent(TaskRunTransitionEventType.READY, taskAttempt.getId(), null));
                        } catch (Exception e) {
                            logger.warn("take taskAttempt = {} failed", taskAttempt.getId(), e);
                        }
                    }
                } catch (Throwable e) {
                    logger.error("failed to take taskAttempt from queue", e);
                }

            }
        }
    }

}
