package com.miotech.kun.workflow.executor.local;

import com.google.common.eventbus.EventBus;
import com.google.inject.Injector;
import com.miotech.kun.commons.utils.Props;
import com.miotech.kun.workflow.common.exception.EntityNotFoundException;
import com.miotech.kun.workflow.common.operator.dao.OperatorDao;
import com.miotech.kun.workflow.common.resource.ResourceLoader;
import com.miotech.kun.workflow.common.taskrun.dao.TaskRunDao;
import com.miotech.kun.workflow.common.taskrun.service.TaskRunService;
import com.miotech.kun.workflow.core.Executor;
import com.miotech.kun.workflow.core.event.TaskAttemptFinishedEvent;
import com.miotech.kun.workflow.core.execution.ExecCommand;
import com.miotech.kun.workflow.core.execution.HeartBeatMessage;
import com.miotech.kun.workflow.core.execution.OperatorReport;
import com.miotech.kun.workflow.core.execution.TaskAttemptMsg;
import com.miotech.kun.workflow.core.model.operator.Operator;
import com.miotech.kun.workflow.core.model.taskrun.TaskAttempt;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunStatus;
import com.miotech.kun.workflow.executor.WorkerFactory;
import com.miotech.kun.workflow.executor.local.thread.TaskAttemptSiftingAppender;
import com.miotech.kun.workflow.utils.DateTimeUtils;
import com.miotech.kun.workflow.worker.Worker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.*;

@Singleton
public class LocalExecutor implements Executor {
    private static final Logger logger = LoggerFactory.getLogger(LocalExecutor.class);

    private static final int QUEUE_SIZE = 20000;

    private final Injector injector;

    @Named("localExecutor.threadPool.coreSize")
    private final Integer coreSize = Runtime.getRuntime().availableProcessors() * 4;

    private final TaskRunService taskRunService;

    private final ResourceLoader resourceLoader;

    private final TaskRunDao taskRunDao;

    private final OperatorDao operatorDao;

    private final MiscService miscService;

    private final EventBus eventBus;

    private final Props props;

    private Map<Long, HeartBeatMessage> workerPool;//key:taskAttemptId,value:HeartBeatMessage

    private final WorkerFactory workerFactory;

    private ArrayBlockingQueue<TaskAttempt> taskAttemptQueue = new ArrayBlockingQueue<>(QUEUE_SIZE);

    private final Long HEARTBEAT_INTERVAL = 5 * 1000L;

    @Inject
    public LocalExecutor(Injector injector, TaskRunService taskRunService, ResourceLoader resourceLoader,
                         TaskRunDao taskRunDao, OperatorDao operatorDao, MiscService miscService,
                         EventBus eventBus, Props props, WorkerFactory workerFactory) {
        this.injector = injector;
        this.taskRunService = taskRunService;
        this.resourceLoader = resourceLoader;
        this.taskRunDao = taskRunDao;
        this.operatorDao = operatorDao;
        this.miscService = miscService;
        this.eventBus = eventBus;
        this.props = props;
        this.workerFactory = workerFactory;
        init();
        recover();
    }

    private void init() {
        workerPool = new ConcurrentHashMap<>();
        attachSiftingAppender();
        logger.info("local executor start at :{}", DateTimeUtils.now());
        Thread consumer = new Thread(new TaskAttemptConsumer());
        consumer.start();
        ScheduledExecutorService timer = new ScheduledThreadPoolExecutor(1);
        timer.scheduleAtFixedRate(new HeartBeatCheckTask(),10,1000, TimeUnit.MILLISECONDS);
    }

    @Override
    public boolean submit(TaskAttempt taskAttempt) {
        return submit(taskAttempt, false);
    }

    public synchronized boolean submit(TaskAttempt taskAttempt, boolean reSubmit) {
        logger.info("submit taskAttemptId = {} to local executor ", taskAttempt.getId());
        Optional<TaskAttempt> taskAttemptOptional = taskRunDao.fetchAttemptById(taskAttempt.getId());
        if (!taskAttemptOptional.isPresent()) {
            logger.error("can not find taskAttempt = {} from database");
            return false;
        } else {
            TaskAttempt savedTaskAttempt = taskAttemptOptional.get();
            if (workerPool.containsKey(taskAttempt.getId())) {
                return false;
            }
            if (!reSubmit && !savedTaskAttempt.getStatus().equals(TaskRunStatus.CREATED)) {
                logger.info("taskAttemptId = {} has been submit", taskAttempt.getId());
                return false;
            }
            taskAttemptQueue.add(taskAttempt);
            if (savedTaskAttempt.getStatus().equals(TaskRunStatus.CREATED)) {
                miscService.changeTaskAttemptStatus(taskAttempt.getId(), TaskRunStatus.QUEUED);
            }
        }
        return true;
    }

    private void startWorker(ExecCommand command) {
        Worker worker = workerFactory.createWorker();
        worker.start(command);
        miscService.changeTaskAttemptStatus(command.getTaskAttemptId(), TaskRunStatus.SUBMIT);
    }

    //rpc
    public boolean statusUpdate(TaskAttemptMsg attemptMsg) {
        logger.info("taskAttempt status change attemptMsg = {}", attemptMsg);
        TaskRunStatus taskRunStatus = attemptMsg.getTaskRunStatus();
        miscService.changeTaskAttemptStatus(attemptMsg.getTaskAttemptId(),
                taskRunStatus, attemptMsg.getStartAt(), attemptMsg.getEndAt());
        if (taskRunStatus.isFinished()) {
            workerPool.remove(attemptMsg.getTaskAttemptId());
            notifyFinished(attemptMsg.getTaskAttemptId(), taskRunStatus, attemptMsg.getOperatorReport());
        }
        if (taskRunStatus.isSuccess()) {
            processReport(attemptMsg.getTaskRunId(), attemptMsg.getOperatorReport());
        }
        return true;
    }

    public boolean heartBeatReceive(HeartBeatMessage heartBeatMessage) {
        logger.info("get heart beat from worker = {}", heartBeatMessage);
        Long taskAttemptId = heartBeatMessage.getTaskAttemptId();
        heartBeatMessage.setLastHeartBeatTime(DateTimeUtils.now());
        if (heartBeatMessage.getTaskRunStatus().isFinished()) {
            Worker worker = workerFactory.getWorker(heartBeatMessage);
            worker.killTask();
            if (workerPool.containsKey(heartBeatMessage.getTaskAttemptId())) {
                workerPool.remove(heartBeatMessage.getTaskAttemptId());
            }
        } else {
            workerPool.put(taskAttemptId, heartBeatMessage);
        }
        return true;
    }

    private void notifyFinished(Long attemptId, TaskRunStatus status, OperatorReport report) {
        TaskAttemptFinishedEvent event = new TaskAttemptFinishedEvent(
                attemptId,
                status,
                report.getInlets(),
                report.getOutlets()
        );
        logger.info("Post taskAttemptFinishedEvent. attemptId={}, event={}", attemptId, event);
        eventBus.post(event);
    }

    private boolean killTaskAttempt(Long attemptId) {
        if (!workerPool.containsKey(attemptId)) {
            return false;
        } else {
            HeartBeatMessage message = workerPool.get(attemptId);
            Worker worker = workerFactory.getWorker(message);
            worker.killTask();
            return true;
        }
    }

    private ExecCommand buildExecCommand(TaskAttempt attempt) {
        long attemptId = attempt.getId();
        String logPath = taskRunService.logPathOfTaskAttempt(attemptId);
        logger.debug("Update logPath to TaskAttempt. attemptId={}, path={}", attemptId, logPath);
        taskRunDao.updateTaskAttemptLogPath(attemptId, logPath);
        // Operator信息
        Long operatorId = attempt.getTaskRun().getTask().getOperatorId();
        Operator operatorDetail = operatorDao.fetchById(operatorId)
                .orElseThrow(EntityNotFoundException::new);
        logger.debug("Fetched operator's details. operatorId={}, details={}", operatorId, operatorDetail);

        ExecCommand command = new ExecCommand();
        command.setRegisterUrl(props.getString("rpc.registry"));
        command.setTaskAttemptId(attemptId);
        command.setTaskRunId(attempt.getTaskRun().getId());
        command.setKeepAlive(false);
        command.setConfig(attempt.getTaskRun().getConfig());
        command.setLogPath(logPath);
        command.setJarPath(operatorDetail.getPackagePath());
        command.setClassName(operatorDetail.getClassName());
        logger.debug("Execute task. attemptId={}, command={}", attemptId, command);
        return command;
    }

    @Override
    public boolean cancel(Long taskAttemptId) {
        return killTaskAttempt(taskAttemptId);
    }


    private void attachSiftingAppender() {
        ch.qos.logback.classic.Logger rootLogger
                = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        TaskAttemptSiftingAppender a = new TaskAttemptSiftingAppender(resourceLoader, taskRunService);
        a.setContext(rootLogger.getLoggerContext());
        a.start();
        rootLogger.addAppender(a);
    }

    private void processReport(Long taskRunId, OperatorReport report) {
        logger.debug("Update task's inlets/outlets. taskRunId={}, inlets={}, outlets={}",
                taskRunId, report.getInlets(), report.getOutlets());
        taskRunDao.updateTaskRunInletsOutlets(taskRunId,
                report.getInlets(), report.getOutlets());
    }

    @PostConstruct
    public void recover() {
        List<TaskAttempt> taskAttemptList = taskRunDao.fetchUnStartedTaskAttemptList();
        logger.debug("recover taskAttempt count = {}", taskAttemptList.size());
        logger.info("recover taskAttempt = {}", taskAttemptList);
        taskAttemptList.forEach(taskAttempt -> submit(taskAttempt, true));
    }

    class TaskAttemptConsumer implements Runnable {
        @Override
        public void run() {
            try {
                Thread.sleep(3 * HEARTBEAT_INTERVAL);
            } catch (InterruptedException e) {
                logger.error("try to sleep failed", e);
            }
            while (true) {
                try {
                    TaskAttempt taskAttempt = taskAttemptQueue.take();
                    if (workerPool.containsKey(taskAttempt.getId())) {
                        return;
                    }
                    ExecCommand command = buildExecCommand(taskAttempt);
                    startWorker(command);
                } catch (InterruptedException e) {
                    logger.error("failed to take taskAttempt from queue", e);
                }

            }
        }
    }


    class HeartBeatCheckTask implements Runnable {

        private final int TIMEOUT_LIMIT = 3;


        @Override
        public void run() {
            OffsetDateTime currentTime = DateTimeUtils.now();
            for (Map.Entry<Long, HeartBeatMessage> entry : workerPool.entrySet()) {
                HeartBeatMessage heartBeatMessage = entry.getValue();
                long taskAttemptId = entry.getKey();
                int timeoutTimes = heartBeatMessage.getTimeoutTimes();
                OffsetDateTime nextHeartbeat = heartBeatMessage.getLastHeartBeatTime().plus(HEARTBEAT_INTERVAL * (timeoutTimes + 1), ChronoUnit.MILLIS);
                if (currentTime.isAfter(nextHeartbeat)) {
                    timeoutTimes++;
                    if (timeoutTimes >= TIMEOUT_LIMIT) {
                        logger.error("heart beat from worker = {} timeout ,taskAttemptId = {} ,remove worker", heartBeatMessage, taskAttemptId);
                        workerPool.remove(taskAttemptId);
                        miscService.changeTaskAttemptStatus(taskAttemptId, TaskRunStatus.FAILED);
                    } else {
                        heartBeatMessage.setTimeoutTimes(timeoutTimes);
                        workerPool.put(taskAttemptId, heartBeatMessage);
                        logger.info("worker = {} timeout = {}", heartBeatMessage, timeoutTimes);
                    }
                }


            }
        }
    }
}
