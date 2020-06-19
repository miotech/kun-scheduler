package com.miotech.kun.workflow.common.taskrun.service;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.miotech.kun.commons.utils.ExceptionUtils;
import com.miotech.kun.workflow.common.exception.EntityNotFoundException;
import com.miotech.kun.workflow.common.resource.ResourceLoader;
import com.miotech.kun.workflow.common.taskrun.bo.TaskAttemptProps;
import com.miotech.kun.workflow.common.taskrun.dao.TaskRunDao;
import com.miotech.kun.workflow.common.taskrun.factory.TaskRunLogVOFactory;
import com.miotech.kun.workflow.common.taskrun.factory.TaskRunStateVOFactory;
import com.miotech.kun.workflow.common.taskrun.filter.TaskRunSearchFilter;
import com.miotech.kun.workflow.common.taskrun.vo.TaskRunLogVO;
import com.miotech.kun.workflow.common.taskrun.vo.TaskRunStateVO;
import com.miotech.kun.workflow.common.taskrun.vo.TaskRunVO;
import com.miotech.kun.workflow.core.model.taskrun.TaskAttempt;
import com.miotech.kun.workflow.core.model.taskrun.TaskRun;
import com.miotech.kun.workflow.core.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Singleton
public class TaskRunService {

    private final Logger logger = LoggerFactory.getLogger(TaskRunService.class);

    @Inject
    private TaskRunDao taskRunDao;

    @Inject
    private ResourceLoader resourceLoader;

    public Optional<TaskRunVO> getTaskRunDetail(Long taskRunId) {
        Optional<TaskRun> taskRun = taskRunDao.fetchTaskRunById(taskRunId);
        return taskRun.map(this::convertToVO);
    }

    public Optional<TaskRunStateVO> getTaskStatus(Long taskRunId) {
        Optional<TaskRun> taskRun = taskRunDao.fetchTaskRunById(taskRunId);
        return taskRun.map(x -> TaskRunStateVOFactory.create(x.getStatus()));
    }

    public TaskRunLogVO getTaskRunLog(final Long taskRunId,
                                      final int attempt,
                                      final long startLine,
                                      final long endLine) {
        Preconditions.checkArgument(startLine >=0, "startLine should larger or equal to 0");
        Preconditions.checkArgument(endLine >= startLine, "endLine should not smaller than startLine");

        List<TaskAttemptProps> attempts = taskRunDao.fetchAttemptsPropByTaskRunId(taskRunId);
        Preconditions.checkArgument(!attempts.isEmpty(), "No valid task attempt found for TaskRun \"%s\"", taskRunId);

        TaskAttemptProps taskAttempt;
        if (attempt > 0) {
            taskAttempt = attempts.stream()
                    .filter(x -> x.getAttempt() == attempt)
                    .findFirst()
                    .orElseThrow(() -> new EntityNotFoundException("Cannot find log for attempt " + attempt ));
        } else {
            attempts.sort((o1, o2) -> o1.getAttempt() < o2.getAttempt() ? 1 : -1);
            taskAttempt = attempts.get(0);
        }
        Resource resource = resourceLoader.getResource(taskAttempt.getLogPath());

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream()))) {

            String line = "";
            List<String> logs = new ArrayList<>();
            long i = 0;
            for (; i <= endLine && (line = reader.readLine()) != null; i++) {
                if (i >= startLine) {
                    logs.add(line);
                }
            }
            return TaskRunLogVOFactory.create(taskRunId, taskAttempt.getAttempt(), startLine, i - 1, logs);
        } catch (IOException e) {
            logger.error("Failed to get task attempt log: {}", taskAttempt.getLogPath(), e);
            throw ExceptionUtils.wrapIfChecked(e);
        }
    }

    public TaskRunVO convertToVO(TaskRun taskRun) {
        List<TaskAttempt> attempts = taskRunDao.fetchAttemptsPropByTaskRunId(taskRun.getId())
                .stream()
                .map(props -> TaskAttempt.newBuilder()
                        .withId(props.getId())
                        // TODO: recheck this
                        .withTaskRun(null)
                        .withLogPath(props.getLogPath())
                        .withStatus(props.getStatus())
                        .withStartAt(props.getStartAt())
                        .withEndAt(props.getEndAt())
                        .withAttempt(props.getAttempt())
                        .build()
                )
                .collect(Collectors.toList());

        return TaskRunVO.newBuilder()
                .withTask(taskRun.getTask())
                .withId(taskRun.getId())
                .withScheduledTick(taskRun.getScheduledTick())
                .withStatus(taskRun.getStatus())
                .withInlets(taskRun.getInlets())
                .withOutlets(taskRun.getOutlets())
                .withDependencyTaskRunIds(taskRun.getDependentTaskRunIds())
                .withStartAt(taskRun.getStartAt())
                .withEndAt(taskRun.getEndAt())
                .withVariables(taskRun.getVariables())
                .withAttempts(attempts)
                .build();
    }

    public List<TaskRun> getUpstreamTaskRuns(TaskRun taskRun, int distance) {
        return taskRunDao.fetchUpstreamTaskRunsById(taskRun.getId(), distance, false);
    }

    public List<TaskRun> getDownstreamTaskRuns(TaskRun taskRun, int distance) {
        return taskRunDao.fetchDownstreamTaskRunsById(taskRun.getId(), distance, false);
    }

    public List<TaskRun> searchTaskRuns(TaskRunSearchFilter filter) {
        return taskRunDao.fetchTaskRunsByFilter(filter);
    }
}
