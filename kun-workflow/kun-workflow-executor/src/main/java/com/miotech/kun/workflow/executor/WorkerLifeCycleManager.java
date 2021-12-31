package com.miotech.kun.workflow.executor;

import com.google.common.eventbus.EventBus;
import com.google.common.util.concurrent.Uninterruptibles;
import com.miotech.kun.commons.pubsub.subscribe.EventSubscriber;
import com.miotech.kun.commons.utils.InitializingBean;
import com.miotech.kun.commons.utils.Props;
import com.miotech.kun.workflow.common.taskrun.bo.TaskAttemptProps;
import com.miotech.kun.workflow.common.taskrun.dao.TaskRunDao;
import com.miotech.kun.workflow.core.event.CheckResultEvent;
import com.miotech.kun.workflow.core.event.TaskRunTransitionEvent;
import com.miotech.kun.workflow.core.event.TaskRunTransitionEventType;
import com.miotech.kun.workflow.core.model.task.CheckType;
import com.miotech.kun.workflow.core.model.taskrun.TaskAttempt;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunStatus;
import com.miotech.kun.workflow.core.model.worker.WorkerInstance;
import com.miotech.kun.workflow.core.model.worker.WorkerSnapshot;
import com.miotech.kun.workflow.executor.local.MiscService;
import com.miotech.kun.workflow.utils.DateTimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private final EventBus eventBus;
    private final EventSubscriber eventSubscriber;
    private Thread consumer = new Thread(new TaskAttemptConsumer(), "TaskAttemptConsumer");

    public WorkerLifeCycleManager(TaskRunDao taskRunDao, WorkerMonitor workerMonitor,
                                  Props props, MiscService miscService,
                                  AbstractQueueManager queueManager, EventBus eventBus,
                                  EventSubscriber eventSubscriber) {
        this.props = props;
        this.taskRunDao = taskRunDao;
        this.workerMonitor = workerMonitor;
        this.miscService = miscService;
        this.queueManager = queueManager;
        this.eventBus = eventBus;
        this.eventSubscriber = eventSubscriber;
    }


    /* ----------- public methods ------------ */
    @Override
    public void afterPropertiesSet() {
        init();
    }

    protected void init() {
        queueManager.init();
        consumer.start();
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
    public String getLog(Long taskAttemptId, Integer tailLines) {
        return getWorkerLog(taskAttemptId, tailLines);
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
                sendAbortEvent(taskAttemptId);
                return;
            } else if (taskAttempt.getStatus().equals(TaskRunStatus.QUEUED)) {
                queueManager.remove(taskAttempt);
                sendAbortEvent(taskAttemptId);
                return;
            } else {
                throw new IllegalStateException("unable to stop taskRun with status: " + taskAttempt.getStatus());
            }
        }
        if (!stopWorker(taskAttemptId)) {
            throw new IllegalStateException("stop worker failed");
        }
        sendAbortEvent(taskAttemptId);
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
        TaskRunTransitionEvent taskRunTransitionEvent = new TaskRunTransitionEvent(TaskRunTransitionEventType.EXCEPTION, taskAttemptId);
        eventBus.post(taskRunTransitionEvent);
    }

    private void sendRunningEvent(Long taskAttemptId) {
        TaskRunTransitionEvent taskRunTransitionEvent = new TaskRunTransitionEvent(TaskRunTransitionEventType.RUNNING, taskAttemptId);
        eventBus.post(taskRunTransitionEvent);
    }

    private void sendCheckEvent(Long taskAttemptId) {
        TaskRunTransitionEvent taskRunTransitionEvent = new TaskRunTransitionEvent(TaskRunTransitionEventType.CHECK, taskAttemptId);
        eventBus.post(taskRunTransitionEvent);
    }

    private void sendFailedEvent(Long taskAttemptId) {
        TaskRunTransitionEvent taskRunTransitionEvent = new TaskRunTransitionEvent(TaskRunTransitionEventType.FAILED, taskAttemptId);
        eventBus.post(taskRunTransitionEvent);
    }

    private void sendCheckFailedEvent(Long taskAttemptId) {
        TaskRunTransitionEvent taskRunTransitionEvent = new TaskRunTransitionEvent(TaskRunTransitionEventType.CHECK_FAILED, taskAttemptId);
        eventBus.post(taskRunTransitionEvent);
    }

    private void sendCheckSuccessEvent(Long taskAttemptId) {
        TaskRunTransitionEvent taskRunTransitionEvent = new TaskRunTransitionEvent(TaskRunTransitionEventType.CHECK_SUCCESS, taskAttemptId);
        eventBus.post(taskRunTransitionEvent);
    }

    private void sendAbortEvent(Long taskAttemptId) {
        TaskRunTransitionEvent taskRunTransitionEvent = new TaskRunTransitionEvent(TaskRunTransitionEventType.ABORT, taskAttemptId);
        eventBus.post(taskRunTransitionEvent);
    }

    private void check(WorkerSnapshot workerSnapshot) {
        TaskAttempt taskAttempt = taskRunDao.fetchAttemptById(workerSnapshot.getIns().getTaskAttemptId()).get();
        logger.debug("taskAttempt = {} going to check", taskAttempt.getId());
        CheckType checkType = taskAttempt.getTaskRun().getTask().getCheckType();
        logger.debug("taskAttemptId = {},checkType is {}", taskAttempt.getId(), checkType.name());
        sendCheckEvent(taskAttempt.getId());
        switch (checkType) {
            case SKIP:
                sendCheckSuccessEvent(taskAttempt.getId());
                break;
            case WAIT_EVENT:
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
        logger.debug("taskAttempt = {} going to  retry,next retry times = {}", taskAttempt.getId(), taskAttempt.getRetryTimes() + 1);
        TaskAttempt retryTaskAttempt = taskAttempt.cloneBuilder()
                .withRetryTimes(taskAttempt.getRetryTimes() + 1)
                .build();
        taskRunDao.updateAttempt(retryTaskAttempt);
        Integer retryDelay = taskAttempt.getTaskRun().getTask().getRetryDelay();
        //sleep to delay
        if (retryDelay != null && retryDelay > 0) {
            logger.debug("taskAttempt = {} wait {}s for  retry", taskAttempt.getId(), retryDelay);
            Uninterruptibles.sleepUninterruptibly(retryDelay, TimeUnit.SECONDS);
        }
        //retry attempt
        queueManager.submit(taskAttempt);
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
                sendRunningEvent(taskAttemptId);
                return;
            }
            // worker is not running ,clean up
            cleanupWorker(workerSnapshot.getIns());

            if (isFailed(workerSnapshot)) {
                if (!retryTaskAttemptIfNecessary(workerSnapshot.getIns())) {
                    sendFailedEvent(taskAttemptId);
                }
            } else if (isSuccess(workerSnapshot)) {
                check(workerSnapshot);
            } else {
                throw new IllegalStateException("illegal worker status");
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
            while (true) {
                if (Thread.currentThread().isInterrupted()) {
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
                } catch (Throwable e) {
                    logger.error("failed to take taskAttempt from queue", e);
                }

            }
        }
    }

}
