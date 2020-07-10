package com.miotech.kun.workflow.executor.local;

import com.google.common.eventbus.EventBus;
import com.miotech.kun.workflow.common.operator.dao.OperatorDao;
import com.miotech.kun.workflow.common.resource.ResourceLoader;
import com.miotech.kun.workflow.common.taskrun.dao.TaskRunDao;
import com.miotech.kun.workflow.core.event.TaskAttemptFinishedEvent;
import com.miotech.kun.workflow.core.execution.Operator;
import com.miotech.kun.workflow.core.execution.OperatorContext;
import com.miotech.kun.workflow.core.execution.TaskAttemptReport;
import com.miotech.kun.workflow.core.model.taskrun.TaskAttempt;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunStatus;
import com.miotech.kun.workflow.core.resource.Resource;
import com.miotech.kun.workflow.utils.DateTimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.net.URL;
import java.net.URLClassLoader;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

public class TaskInProgress implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(TaskInProgress.class);

    private final TaskAttempt attempt;

    private volatile Thread thread;

    @Inject
    private TaskRunDao taskRunDao;

    @Inject
    private OperatorDao operatorDao;

    @Inject
    private ResourceLoader resourceLoader;

    @Inject
    private CommonService commonService;

    @Inject
    private EventBus eventBus;

    public TaskInProgress(TaskAttempt attempt) {
        this.attempt = attempt;
    }

    @Override
    public void run() {
        try {
            long attemptId = attempt.getId();
            thread = Thread.currentThread();

            // 更新任务状态为RUNNING，开始时间
            OffsetDateTime startAt = DateTimeUtils.now();
            logger.debug("Change TaskAttempt's status to RUNNING. taskAttempt={}, startAt={}", attempt, startAt);
            commonService.changeTaskAttemptStatus(attemptId, TaskRunStatus.RUNNING, startAt, null);

            // 初始化上下文
            String logPath = newLogPath(attemptId);
            Resource logResource = resourceLoader.getResource(logPath, true);
            OperatorContext context = initContext(logResource);

            logger.debug("Update logPath to TaskAttempt. attemptId={}, path={}", attemptId, logPath);
            taskRunDao.updateTaskAttemptLogPath(attemptId, logPath);

            // Operator信息
            Long operatorId = attempt.getTaskRun().getTask().getOperatorId();
            com.miotech.kun.workflow.core.model.operator.Operator operatorDetail = operatorDao.fetchById(operatorId).get();
            logger.debug("Fetched operator's details. operatorId={}, details={}", operatorId, operatorDetail);

            TaskRunStatus finalStatus;
            TaskAttemptReport report = TaskAttemptReport.BLANK;

            ClassLoader origCtxCl = thread.getContextClassLoader();
            try {
                // 加载Operator
                Operator operator = loadOperator(operatorDetail.getPackagePath(), operatorDetail.getClassName());
                operator.setContext(context);
                logger.debug("Loaded operator's class. operatorId={}", operatorId);

                // 设置Operator的ClassLoader为ContextClassLoader
                thread.setContextClassLoader(operator.getClass().getClassLoader());

                // 初始化Operator
                operator.init();

                // 运行
                logger.debug("Run operator. operatorId={}", operatorId);
                boolean success = operator.run();
                logger.debug("Operator execution finished. operatorId={}, success={}", operatorId, success);

                finalStatus = success ? TaskRunStatus.SUCCESS : TaskRunStatus.FAILED;
                report = operator.getReport().orElse(TaskAttemptReport.BLANK);
            } catch (Throwable e) {
                logger.debug("Operator execution terminated. operatorId={}", operatorId, e);
                context.getLogger().error("Unexpected exception occurred. OperatorName={}, TaskRunId={}",
                        operatorDetail.getName(), attempt.getTaskRun().getId(), e);
                finalStatus = TaskRunStatus.FAILED;
            } finally {
                thread.setContextClassLoader(origCtxCl);
            }

            // 更新任务状态为SUCCESS/FAILED，结束时间
            OffsetDateTime endAt = DateTimeUtils.now();
            logger.debug("Change TaskAttempt's status to {}. taskAttempt={}, endAt={}", finalStatus, attempt, endAt);
            commonService.changeTaskAttemptStatus(attemptId, finalStatus, null, endAt);

            // 处理Report内容（输入/输出）
            if (finalStatus.isSuccess()) {
                processReport(attempt, report);
            }

            // 通知任务已完成
            notifyFinished(attempt, finalStatus, report);
        } catch (Throwable ex) {
            logger.error("Unexpected exception occurs in operator execution. attemptId={}", attempt.getId(), ex);
        }
    }

    private Operator loadOperator(String jarPath, String mainClass) {
        try {
            // TODO: 使用Resource接口读取Jar
            URL[] urls = {new URL("jar:" + jarPath + "!/")};
            URLClassLoader cl = URLClassLoader.newInstance(urls, getClass().getClassLoader());
            Class clazz = Class.forName(mainClass, true, cl);
            if (!Operator.class.isAssignableFrom(clazz)) {
                throw new IllegalArgumentException(mainClass + " is not a valid Operator class.");
            }
            return (Operator) clazz.newInstance();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load jar. jarPath=" + jarPath, e);
        }
    }

    private OperatorContext initContext(Resource logResource) {
        return new OperatorContextImpl(attempt, logResource);
    }

    private String newLogPath(long attemptId) {
        String date = DateTimeUtils.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return String.format("file:logs/%s/%s", date, attemptId);
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
