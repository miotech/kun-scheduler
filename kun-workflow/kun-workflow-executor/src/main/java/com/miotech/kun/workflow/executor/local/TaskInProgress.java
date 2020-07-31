package com.miotech.kun.workflow.executor.local;

import com.google.common.eventbus.EventBus;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import com.miotech.kun.workflow.common.exception.EntityNotFoundException;
import com.miotech.kun.workflow.common.operator.dao.OperatorDao;
import com.miotech.kun.workflow.common.operator.service.OperatorService;
import com.miotech.kun.workflow.common.resource.ResourceLoader;
import com.miotech.kun.workflow.common.taskrun.dao.TaskRunDao;
import com.miotech.kun.workflow.common.taskrun.service.TaskRunService;
import com.miotech.kun.workflow.core.event.TaskAttemptFinishedEvent;
import com.miotech.kun.workflow.core.execution.KunOperator;
import com.miotech.kun.workflow.core.execution.OperatorContext;
import com.miotech.kun.workflow.core.execution.TaskAttemptReport;
import com.miotech.kun.workflow.core.model.taskrun.TaskAttempt;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunStatus;
import com.miotech.kun.workflow.utils.DateTimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.net.URLClassLoader;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;

import static com.miotech.kun.workflow.executor.local.TaskAttemptSiftingAppender.SIFTING_KEY;

public class TaskInProgress implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(TaskInProgress.class);

    private final TaskAttempt attempt;

    private Thread runThread;
    private Thread abortThread;

    private ListenableFuture<Void> runFuture;

    private AtomicReference<TaskRunStatus> state;

    private KunOperator operator;

    @Inject
    private TaskRunDao taskRunDao;

    @Inject
    private TaskRunService taskRunService;

    @Inject
    private OperatorDao operatorDao;

    @Inject
    private OperatorService operatorService;

    @Inject
    private ResourceLoader resourceLoader;

    @Inject
    private MiscService miscService;

    @Inject
    private EventBus eventBus;

    public TaskInProgress(TaskAttempt attempt) {
        this.attempt = attempt;
        this.state = new AtomicReference<>(TaskRunStatus.CREATED);
    }

    @Override
    public void run() {
        try {
            // 初始化
            long attemptId = attempt.getId();
            runThread = Thread.currentThread();

            OperatorContext context = initContext();
            String logPath = taskRunService.logPathOfTaskAttempt(attemptId);
            logger.debug("Update logPath to TaskAttempt. attemptId={}, path={}", attemptId, logPath);
            taskRunDao.updateTaskAttemptLogPath(attemptId, logPath);

            OffsetDateTime startAt = DateTimeUtils.now();

            // 如果当前状态是ABORTING，则退出
            if (!compareAndSetState(TaskRunStatus.QUEUED, TaskRunStatus.RUNNING)) {
                if (getState() == TaskRunStatus.ABORTING) {
                    taskRunDao.updateTaskAttemptExecutionTime(attemptId, startAt, null);
                    return;
                } else {
                    throw new IllegalStateException("Unexpected state: " + getState());
                }
            }

            // 更新任务状态为RUNNING，开始时间
            logger.debug("Change TaskAttempt's status to RUNNING. taskAttempt={}, startAt={}", attempt, startAt);
            miscService.changeTaskAttemptStatus(attemptId, getState(), startAt, null);

            // Operator信息
            Long operatorId = attempt.getTaskRun().getTask().getOperatorId();
            com.miotech.kun.workflow.core.model.operator.Operator operatorDetail = operatorDao.fetchById(operatorId)
                    .orElseThrow(EntityNotFoundException::new);
            logger.debug("Fetched operator's details. operatorId={}, details={}", operatorId, operatorDetail);

            TaskAttemptReport report = TaskAttemptReport.BLANK;
            ClassLoader origCtxCl = runThread.getContextClassLoader();
            KunOperator runOperator = null;

            MDC.MDCCloseable mdcc = null;
            try {
                mdcc = MDC.putCloseable(SIFTING_KEY, String.valueOf(attemptId));

                // 加载Operator
                runOperator = operatorService.loadOperator(operatorId);
                runOperator.setContext(context);
                this.operator = runOperator;
                logger.debug("Loaded operator's class. operatorId={}", operatorId);

                // 设置Operator的ClassLoader为ContextClassLoader
                runThread.setContextClassLoader(runOperator.getClass().getClassLoader());

                if (getState() == TaskRunStatus.ABORTING) {
                    return;
                }

                // 初始化Operator
                runOperator.init();

                if (getState() == TaskRunStatus.ABORTING) {
                    return;
                }

                // 运行
                logger.debug("Run operator. operatorId={}", operatorId);
                boolean success = runOperator.run();
                logger.debug("Operator execution finished. operatorId={}, success={}", operatorId, success);

                if (getState() == TaskRunStatus.ABORTING) {
                    return;
                }

                setState(success ? TaskRunStatus.SUCCESS : TaskRunStatus.FAILED);
                report = runOperator.getReport().orElse(TaskAttemptReport.BLANK);
            } catch (Throwable e) {
                logger.error("Unexpected exception occurred. OperatorName={}, TaskRunId={}",
                        operatorDetail.getName(), attempt.getTaskRun().getId(), e);

                setState(TaskRunStatus.FAILED);
            } finally {
                runThread.setContextClassLoader(origCtxCl);
                if (mdcc != null) {
                    mdcc.close();
                }
            }

            // 更新任务状态为SUCCESS/FAILED，结束时间
            OffsetDateTime endAt = DateTimeUtils.now();
            logger.debug("Change TaskAttempt's status to {}. taskAttempt={}, endAt={}", getState(), attempt, endAt);
            miscService.changeTaskAttemptStatus(attemptId, getState(), null, endAt);

            // 处理Report内容（输入/输出）
            if (getState().isSuccess()) {
                processReport(attempt, report);
            }

            // 通知任务已完成
            notifyFinished(attempt, getState(), report);
        } catch (Throwable ex) {
            logger.error("Unexpected exception occurs in operator execution. attemptId={}", attempt.getId(), ex);
        }
    }

    public synchronized boolean isCompleted() {
        return getState().isFinished();
    }

    /**
     * Abort the task and returns run Future of this task.
     * A new abort thread will be started and trigger onAbort() method of operator
     * @return run Future of this task
     */
    public synchronized Future<Void> abort() {
        setState(TaskRunStatus.ABORTING);
        miscService.changeTaskAttemptStatus(attempt.getId(), TaskRunStatus.ABORTING);

        String name = String.format("Thread-abort-attempt-%s", attempt.getId());
        abortThread = new Thread(() -> {
            operator.abort();
        }, name);
        abortThread.start();

        runFuture.addListener(
                () -> {
                    setState(TaskRunStatus.ABORTED);
                    miscService.changeTaskAttemptStatus(attempt.getId(), TaskRunStatus.ABORTED,
                            null, DateTimeUtils.now());
                },
                MoreExecutors.directExecutor()
        );
        return runFuture;
    }

    /**
     * Enforcing abort on current task in progress
     */
    @SuppressWarnings("java:S1874")
    public synchronized void forceAbort() {
        logger.debug("Enforcing abort on thread {}, {} ...", runThread.getName(), abortThread.getName());
        stopThread(abortThread);
        stopThread(runThread);
        miscService.changeTaskAttemptStatus(attempt.getId(), TaskRunStatus.ABORTED,
                null, DateTimeUtils.now());
    }

    public void changeStatus(TaskRunStatus update, @Nullable OffsetDateTime startAt, @Nullable OffsetDateTime endAt) {
        setState(update);
        miscService.changeTaskAttemptStatus(attempt.getId(), update, startAt, endAt);
    }

    void setRunFuture(ListenableFuture<Void> runFuture) {
        this.runFuture = runFuture;
    }

    private OperatorContext initContext() {
        return new OperatorContextImpl(attempt);
    }

    private void processReport(TaskAttempt attempt, TaskAttemptReport report) {
        logger.debug("Update task's inlets/outlets. taskRunId={}, inlets={}, outlets={}",
                attempt.getTaskRun().getId(), report.getInlets(), report.getOutlets());
        taskRunDao.updateTaskRunInletsOutlets(attempt.getTaskRun().getId(),
                report.getInlets(), report.getOutlets());
    }

    private void notifyFinished(TaskAttempt attempt, TaskRunStatus status, TaskAttemptReport report) {
        TaskAttemptFinishedEvent event = new TaskAttemptFinishedEvent(
                attempt.getId(),
                status,
                report.getInlets(),
                report.getOutlets()
        );
        logger.debug("Post taskAttemptFinishedEvent. attemptId={}, event={}", attempt.getId(), event);
        eventBus.post(event);
    }

    private TaskRunStatus getState() {
        return this.state.get();
    }

    private TaskRunStatus setState(TaskRunStatus state) {
        return this.state.getAndSet(state);
    }

    private boolean compareAndSetState(TaskRunStatus expect, TaskRunStatus update) {
        return this.state.compareAndSet(expect, update);
    }

    private void stopThread(Thread thread) {
        if (thread != null) {
            thread.interrupt();
        }
    }
}
