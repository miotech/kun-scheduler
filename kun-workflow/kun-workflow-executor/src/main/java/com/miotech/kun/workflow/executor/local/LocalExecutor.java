package com.miotech.kun.workflow.executor.local;

import com.google.common.eventbus.EventBus;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.inject.Injector;
import com.miotech.kun.commons.utils.Props;
import com.miotech.kun.workflow.common.exception.EntityNotFoundException;
import com.miotech.kun.workflow.common.lineage.service.LineageService;
import com.miotech.kun.workflow.common.operator.dao.OperatorDao;
import com.miotech.kun.workflow.common.resource.ResourceLoader;
import com.miotech.kun.workflow.common.taskrun.bo.TaskAttemptProps;
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
import com.miotech.kun.workflow.core.model.taskrun.TaskRun;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunStatus;
import com.miotech.kun.workflow.executor.WorkerFactory;
import com.miotech.kun.workflow.executor.local.thread.TaskAttemptSiftingAppender;
import com.miotech.kun.workflow.utils.DateTimeUtils;
import com.miotech.kun.workflow.worker.Worker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.*;

@Singleton
public class LocalExecutor implements Executor {
    private static final Logger logger = LoggerFactory.getLogger(LocalExecutor.class);

    private static final int QUEUE_SIZE = 20000;

    private final Injector injector;

    private final TaskRunService taskRunService;

    private final ResourceLoader resourceLoader;

    private final TaskRunDao taskRunDao;

    private final OperatorDao operatorDao;

    private final MiscService miscService;

    private final EventBus eventBus;

    private final LineageService lineageService;

    private final Integer CORES = Runtime.getRuntime().availableProcessors();

    private final Integer TASK_LIMIT = 2048;

    private final Props props;

    private Map<Long, HeartBeatMessage> workerPool;//key:taskAttemptId,value:HeartBeatMessage

    private final WorkerFactory workerFactory;

    private final Long HEARTBEAT_INTERVAL = 5 * 1000L;

    private final Long WAIT_WORKER_INIT_SECOND = 60L;

    private QueueManage queueManage;

    private ExecutorService workerStarterThreadPool =
            new ThreadPoolExecutor(CORES, CORES * 4,
                    5L, TimeUnit.SECONDS,
                    new LinkedBlockingQueue<>(TASK_LIMIT),
                    new ThreadFactoryBuilder().setNameFormat("local-executor-workerStarter-%d").build());

    @Inject
    public LocalExecutor(Injector injector, TaskRunService taskRunService, ResourceLoader resourceLoader,
                         TaskRunDao taskRunDao, OperatorDao operatorDao, MiscService miscService,
                         EventBus eventBus, Props props, WorkerFactory workerFactory, LineageService lineageService) {
        this.injector = injector;
        this.taskRunService = taskRunService;
        this.resourceLoader = resourceLoader;
        this.taskRunDao = taskRunDao;
        this.operatorDao = operatorDao;
        this.miscService = miscService;
        this.eventBus = eventBus;
        this.props = props;
        this.workerFactory = workerFactory;
        this.lineageService = lineageService;
        queueManage = new QueueManage(props);
        init();
        if (props.getBoolean("executor.enableRecover", true)) {
            recover();
        }
    }

    private void init() {
        workerPool = new ConcurrentHashMap<>();
        attachSiftingAppender();
        logger.info("local executor start at :{}", DateTimeUtils.now());
        Thread consumer = new Thread(new TaskAttemptConsumer());
        consumer.start();
        ScheduledExecutorService timer = new ScheduledThreadPoolExecutor(1);
        timer.scheduleAtFixedRate(new HeartBeatCheckTask(), 10, 1000, TimeUnit.MILLISECONDS);
    }

    @Override
    public synchronized boolean submit(TaskAttempt taskAttempt) {
        return submit(taskAttempt, false);
    }

    public boolean submit(TaskAttempt taskAttempt, boolean reSubmit) {
        logger.info("submit taskAttemptId = {} to local executor ", taskAttempt.getId());
        Optional<TaskAttempt> taskAttemptOptional = taskRunDao.fetchAttemptById(taskAttempt.getId());
        if (!taskAttemptOptional.isPresent()) {
            logger.error("can not find taskAttempt = {} from database", taskAttempt);
            return false;
        } else {
            TaskAttempt savedTaskAttempt = taskAttemptOptional.get();
            if (workerPool.containsKey(taskAttempt.getId())) {
                logger.warn("taskAttemptId = {} exist in worker pool");
                return false;
            }
            if (!reSubmit && !savedTaskAttempt.getStatus().equals(TaskRunStatus.CREATED)) {
                logger.debug("taskAttemptId = {} has been submit", taskAttempt.getId());
                return false;
            }
            queueManage.submit(taskAttempt.cloneBuilder().withStatus(TaskRunStatus.QUEUED).build());
            if (!savedTaskAttempt.getStatus().equals(TaskRunStatus.QUEUED)) {
                miscService.changeTaskAttemptStatus(taskAttempt.getId(), TaskRunStatus.QUEUED);

            }
        }
        return true;
    }

    private void startWorker(ExecCommand command) {
        Worker worker = workerFactory.createWorker();
        miscService.changeTaskAttemptStatus(command.getTaskAttemptId(), TaskRunStatus.INITIALIZING);
        worker.start(command);
    }

    //rpc
    public boolean statusUpdate(TaskAttemptMsg attemptMsg) {
        logger.info("taskAttempt status change attemptMsg = {}", attemptMsg);
        TaskRunStatus taskRunStatus = attemptMsg.getTaskRunStatus();
        if (taskRunStatus.isFinished()) {
            if (workerPool.containsKey(attemptMsg.getTaskAttemptId())) {
                logger.info("taskAttemptId = {} going to release", attemptMsg.getTaskAttemptId());
                queueManage.release(attemptMsg.getQueueName());
            }
            workerPool.remove(attemptMsg.getTaskAttemptId());
            logger.info("remove taskAttemptId = {} from worker pool", attemptMsg.getTaskAttemptId());
            notifyFinished(attemptMsg.getTaskAttemptId(), taskRunStatus, attemptMsg.getOperatorReport());
        }
        if (taskRunStatus.isSuccess()) {
            processReport(attemptMsg.getTaskRunId(), attemptMsg.getOperatorReport());
        }
        miscService.changeTaskAttemptStatus(attemptMsg.getTaskAttemptId(),
                taskRunStatus, attemptMsg.getStartAt(), attemptMsg.getEndAt());
        return true;
    }

    public boolean heartBeatReceive(HeartBeatMessage heartBeatMessage) {
        logger.info("get heart beat from worker = {}", heartBeatMessage);
        Long taskAttemptId = heartBeatMessage.getTaskAttemptId();
        heartBeatMessage.setLastHeartBeatTime(DateTimeUtils.now());
        logger.info("update taskAttemptId = {} to worker pool", taskAttemptId);
        workerPool.put(taskAttemptId, heartBeatMessage);
        TaskAttemptProps taskAttemptProps = taskRunDao.fetchLatestTaskAttempt(heartBeatMessage.getTaskRunId());
        if (taskAttemptProps.getStatus().isFinished()) {
            Worker worker = workerFactory.getWorker(heartBeatMessage);
            worker.killTask(false);
            workerPool.remove(heartBeatMessage.getTaskAttemptId());
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
        logger.debug("going to abort taskAttempt,attemptId = {}", attemptId);
        if (!workerPool.containsKey(attemptId)) {
            Optional<TaskAttempt> taskAttemptOptional = taskRunDao.fetchAttemptById(attemptId);
            if (!taskAttemptOptional.isPresent()) {
                logger.warn("taskAttempt not found,attemptId = {}", attemptId);
                return false;
            }
            TaskAttempt taskAttempt = taskAttemptOptional.get();
            if (taskAttempt.getStatus().isFinished()) {
                logger.warn("taskAttempt has finished, taskAttemptId = {}", attemptId);
                return false;
            }
            if (taskAttempt.getStatus() == TaskRunStatus.QUEUED) {
                logger.debug("taskAttempt to be abort has add to queue,attemptId = {}", attemptId);
                queueManage.remove(taskAttempt);
            }
            miscService.changeTaskAttemptStatus(attemptId, TaskRunStatus.ABORTED);
        } else {
            HeartBeatMessage message = workerPool.get(attemptId);
            Worker worker = workerFactory.getWorker(message);
            worker.killTask(true);
            Thread thread = new Thread(new WaitAbort(attemptId));
            thread.start();
        }
        return true;
    }

    private ExecCommand buildExecCommand(TaskAttempt attempt) {
        Long attemptId = attempt.getId();
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
        command.setQueueName(attempt.getQueueName());
        logger.debug("Execute task. attemptId={}, command={}", attemptId, command);
        return command;
    }

    @Override
    public boolean cancel(Long taskAttemptId) {
        return killTaskAttempt(taskAttemptId);
    }

    @Override
    public void changePriority(String queueName, long attemptId, int priority) {
        queueManage.changePriority(queueName, attemptId, priority);
    }

    @Override
    public boolean reset() {
        logger.info("executor going to shutdown");
        queueManage.reset();
        clear();
        return true;
    }

    private synchronized void clear() {
        workerPool.clear();
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
        TaskRun taskRun = taskRunDao.fetchTaskRunById(taskRunId).get();
        lineageService.updateTaskLineage(taskRun.getTask(), report.getInlets(), report.getOutlets());

    }

    public boolean recover() {
        List<TaskAttempt> taskAttemptList = taskRunDao.fetchUnStartedTaskAttemptList();
        logger.debug("recover taskAttempt to queue count = {}", taskAttemptList.size());
        List<TaskAttempt> runningTaskAttemptList = taskRunDao.fetchRunningTaskAttemptList();
        logger.debug("recover taskAttempt to workerPool count = {}", runningTaskAttemptList.size());
        for (TaskAttempt taskAttempt : runningTaskAttemptList) {
            workerStarterThreadPool.submit(new WorkerStartRunner(taskAttempt));
        }
        for (int i = 0; i < taskAttemptList.size(); i++) {
            submit(taskAttemptList.get(i), true);
        }
        return true;
    }

    private HeartBeatMessage initHeartBeatByTaskAttempt(TaskAttempt taskAttempt) {
        HeartBeatMessage message = new HeartBeatMessage();
        message.setQueueName(taskAttempt.getQueueName());
        message.setTimeoutTimes(0);
        message.setTaskAttemptId(taskAttempt.getId());
        message.setTaskRunId(taskAttempt.getTaskRun().getId());
        message.setTaskRunStatus(taskAttempt.getStatus());
        message.setInitTime(DateTimeUtils.now().plusSeconds(WAIT_WORKER_INIT_SECOND));//初始化1分钟
        return message;
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
                    TaskAttempt taskAttempt = queueManage.take();
                    if (workerPool.containsKey(taskAttempt.getId())) {
                        logger.warn("taskAttemptId = {},exist in workerPool", taskAttempt.getId());
                        return;
                    }
                    workerStarterThreadPool.submit(new WorkerStartRunner(taskAttempt));
                } catch (InterruptedException e) {
                    logger.error("failed to take taskAttempt from queue", e);
                }

            }
        }
    }

    class WorkerStartRunner implements Runnable {

        private TaskAttempt taskAttempt;

        public WorkerStartRunner(TaskAttempt taskAttempt) {
            this.taskAttempt = taskAttempt;
        }

        @Override
        public void run() {
            TaskAttempt taskAttemptToRun = taskRunDao.fetchAttemptById(taskAttempt.getId()).get();
            if (taskAttemptToRun.getStatus().isFinished()) {
                logger.info("taskAttemptToRun is finished,attemptId = {},status = {}", taskAttemptToRun.getId(), taskAttemptToRun.getStatus().name());
                queueManage.release(taskAttemptToRun.getQueueName());
                return;
            }
            //taskAttempt 已经启动（重启恢复）,则只加入workerPool监听心跳,正常入队和超时则重新启动
            if (taskAttempt.getStatus().equals(TaskRunStatus.QUEUED) || taskAttempt.getStatus().equals(TaskRunStatus.ERROR)) {
                try {
                    logger.info("taskAttemptId = {},queue = {} going to start ", taskAttempt.getId(), taskAttempt.getQueueName());
                    workerPool.put(taskAttempt.getId(), initHeartBeatByTaskAttempt(taskAttempt));
                    ExecCommand command = buildExecCommand(taskAttempt);
                    startWorker(command);
                    return;
                } catch (Exception e) {
                    logger.error("taskAttemptId = {} could not start worker ", taskAttempt.getId(), e);
                    queueManage.release(taskAttempt.getQueueName());
                    workerPool.remove(taskAttempt.getTaskId());
                    logger.info("remove taskAttemptId = {} from worker pool", taskAttempt.getId());

                }
            }
            logger.info("recover taskAttempt,id = {} , queueName = {}", taskAttempt.getId(), taskAttempt.getQueueName());
            //避免由于同时运行多个docker导致RUNNING的taskRun超过限制引起状态监控混乱
            try {
                queueManage.recover(taskAttempt.getQueueName());
            } catch (IllegalStateException e) {
                logger.error("recover taskAttemptId = {} failed", e);
                logger.info("recover taskAttemptId = {} to queue", taskAttempt.getId());
                submit(taskAttempt, true);
            }
            logger.info("recover taskAttemptId = {} to worker pool", taskAttempt.getId());
            workerPool.put(taskAttempt.getId(), initHeartBeatByTaskAttempt(taskAttempt));


        }
    }


    class HeartBeatCheckTask implements Runnable {

        private final int TIMEOUT_LIMIT = 3;


        @Override
        public void run() {
            OffsetDateTime currentTime = DateTimeUtils.now();
            for (Map.Entry<Long, HeartBeatMessage> entry : workerPool.entrySet()) {
                HeartBeatMessage heartBeatMessage = entry.getValue();
                Long taskAttemptId = entry.getKey();
                int timeoutTimes = heartBeatMessage.getTimeoutTimes();
                OffsetDateTime lastHearBeat = heartBeatMessage.getLastHeartBeatTime() != null ?
                        heartBeatMessage.getLastHeartBeatTime() : heartBeatMessage.getInitTime();
                OffsetDateTime nextHeartbeat = lastHearBeat.
                        plus(HEARTBEAT_INTERVAL * (timeoutTimes + 1), ChronoUnit.MILLIS);
                if (currentTime.isAfter(nextHeartbeat)) {
                    timeoutTimes++;
                    if (timeoutTimes >= TIMEOUT_LIMIT) {
                        logger.error("heart beat from worker timeout ,taskAttemptId = {} ,remove worker", taskAttemptId);
                        handleTimeoutAttempt(taskAttemptId);
                    } else {
                        heartBeatMessage.setTimeoutTimes(timeoutTimes);
                        logger.info("put taskAttemptId = {} to worker pool", taskAttemptId);
                        workerPool.put(taskAttemptId, heartBeatMessage);
                    }
                }


            }
        }
    }

    private void handleTimeoutAttempt(Long taskAttemptId) {
        //kill worker when worker time out
        if (workerPool.containsKey(taskAttemptId)) {
            Worker worker = workerFactory.getWorker(workerPool.get(taskAttemptId));
            worker.shutdown();
            logger.debug("worker is shutdown,taskAttemptId = {}", taskAttemptId);
            HeartBeatMessage message = workerPool.remove(taskAttemptId);
            logger.debug("taskAttempt is removed from worker pool,taskAttemptId = {}", taskAttemptId);
            queueManage.release(message.getQueueName());
            miscService.changeTaskAttemptStatus(taskAttemptId, TaskRunStatus.ERROR);
            TaskAttempt taskAttempt = taskRunDao.fetchAttemptById(taskAttemptId).get();
            submit(taskAttempt, true);
        }
    }

    class WaitAbort implements Runnable {

        private Long taskAttemptId;

        WaitAbort(Long taskAttemptId) {
            this.taskAttemptId = taskAttemptId;
        }

        @Override
        public void run() {
            try {
                Thread.sleep(2 * HEARTBEAT_INTERVAL);
            } catch (InterruptedException e) {
                logger.error("Failed in wait for : {}s", 10, e);
                Thread.currentThread().interrupt();
            }
            if (workerPool.containsKey(taskAttemptId)) {
                logger.info("force kill taskAttempt = {}", taskAttemptId);
                Worker worker = workerFactory.getWorker(workerPool.get(taskAttemptId));
                if (worker.shutdown()) {
                    HeartBeatMessage message = workerPool.remove(taskAttemptId);
                    logger.info("remove taskAttemptId = {} from worker pool", taskAttemptId);
                    logger.debug("taskAttemptId = {} release worker token, queueName = {},", taskAttemptId, message.getQueueName());
                    queueManage.release(message.getQueueName());
                    notifyFinished(taskAttemptId, TaskRunStatus.ABORTED, OperatorReport.BLANK);
                    miscService.changeTaskAttemptStatus(taskAttemptId,
                            TaskRunStatus.ABORTED, null, DateTimeUtils.now());
                } else {
                    logger.error("force abort taskAttempt = {} failed", taskAttemptId);
                }
            }
        }
    }
}
