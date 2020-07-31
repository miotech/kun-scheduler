package com.miotech.kun.workflow.executor.local;

import com.google.common.eventbus.EventBus;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.miotech.kun.workflow.common.exception.EntityNotFoundException;
import com.miotech.kun.workflow.common.operator.dao.OperatorDao;
import com.miotech.kun.workflow.common.taskrun.dao.TaskRunDao;
import com.miotech.kun.workflow.common.taskrun.service.TaskRunService;
import com.miotech.kun.workflow.core.event.TaskAttemptFinishedEvent;
import com.miotech.kun.workflow.core.execution.TaskAttemptReport;
import com.miotech.kun.workflow.core.model.operator.Operator;
import com.miotech.kun.workflow.core.model.taskrun.TaskAttempt;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunStatus;
import com.miotech.kun.workflow.executor.ExecCommand;
import com.miotech.kun.workflow.executor.ExecResult;
import com.miotech.kun.workflow.executor.TaskRunner;
import com.miotech.kun.workflow.executor.local.process.ProcessTaskRunner;
import com.miotech.kun.workflow.utils.DateTimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.time.OffsetDateTime;
import java.util.concurrent.Future;

public class TaskInProgress implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(TaskInProgress.class);

    private final TaskAttempt attempt;

    private TaskRunner taskRunner;

    private ListenableFuture<ExecResult> resultFuture;

    private TaskRunStatus state;

    @Inject
    private TaskRunDao taskRunDao;

    @Inject
    private TaskRunService taskRunService;

    @Inject
    private OperatorDao operatorDao;

    @Inject
    private MiscService miscService;

    @Inject
    private EventBus eventBus;

    public TaskInProgress(TaskAttempt attempt) {
        this.attempt = attempt;
        this.state = TaskRunStatus.CREATED;
    }

    @Override
    public void run() {
        try {
            // 初始化
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
            command.setConfig(attempt.getTaskRun().getConfig());
            command.setLogPath(logPath);
            command.setJarPath(operatorDetail.getPackagePath());
            command.setClassName(operatorDetail.getClassName());
            logger.debug("Execute task. attemptId={}, command={}", attemptId, command);

            OffsetDateTime startAt = DateTimeUtils.now();

            synchronized (this) {
                // 如果当前状态是ABORTING，则退出
                if (state == TaskRunStatus.ABORTING) {
                    logger.info("task is cancelled due to an abortion. attemptId={}", attemptId);
                    changeStatus(TaskRunStatus.ABORTED, startAt, startAt);
                    return;
                }

                // TODO: 用TaskRunnerFactory实现支持"线程级"和"进程级"运行方式
                taskRunner = new ProcessTaskRunner();
                resultFuture = taskRunner.run(command);

                // 更新任务状态为RUNNING，开始时间
                logger.debug("Change TaskAttempt's status to RUNNING. taskAttempt={}, startAt={}", attempt, startAt);
                changeStatus(TaskRunStatus.RUNNING, startAt, null);
            }

            ExecResult result = resultFuture.get();

            TaskRunStatus status;
            if (result.isCancelled()) {
                status = TaskRunStatus.ABORTED;
            } else {
                status = result.isSuccess() ? TaskRunStatus.SUCCESS : TaskRunStatus.FAILED;
            }

            // 更新任务状态为SUCCESS/FAILED，结束时间
            OffsetDateTime endAt = DateTimeUtils.now();
            logger.debug("Change TaskAttempt's status to {}. taskAttempt={}, endAt={}", status, attempt, endAt);
            changeStatus(status, null, endAt);

            // 处理Report内容（输入/输出）
            if (state.isSuccess()) {
                processReport(attempt, result.getReport());
            }

            // 通知任务已完成
            notifyFinished(attempt, state, result.getReport());
        } catch (Throwable ex) {
            logger.error("Unexpected exception occurs in operator execution. attemptId={}", attempt.getId(), ex);
        }
    }

    public synchronized boolean isCompleted() {
        return state.isFinished();
    }

    /**
     * Abort the task and returns run Future of this task.
     * A new abort thread will be started and trigger onAbort() method of operator
     * @return run Future of this task
     */
    public synchronized Future abort() {
        if (state == TaskRunStatus.ABORTING) {
            if (resultFuture != null) {
                return resultFuture;
            } else {
                return Futures.immediateFuture(null);
            }
        }

        changeStatus(TaskRunStatus.ABORTING, null, null);

        // taskRunner does not start running yet
        if (taskRunner == null) {
            return Futures.immediateFuture(null);
        } else {
            taskRunner.abort();
            return resultFuture;
        }
    }

    /**
     * Enforcing abort on current task in progress
     */
    @SuppressWarnings("java:S1874")
    public synchronized void forceAbort() {
        taskRunner.forceAbort();
    }

    public void changeStatus(TaskRunStatus update, @Nullable OffsetDateTime startAt, @Nullable OffsetDateTime endAt) {
        state = update;
        miscService.changeTaskAttemptStatus(attempt.getId(), update, startAt, endAt);
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
}
