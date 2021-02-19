package com.miotech.kun.workflow.common.taskrun.service;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.miotech.kun.commons.utils.ExceptionUtils;
import com.miotech.kun.workflow.common.exception.EntityNotFoundException;
import com.miotech.kun.workflow.common.resource.ResourceLoader;
import com.miotech.kun.workflow.common.task.vo.PaginationVO;
import com.miotech.kun.workflow.common.taskrun.bo.TaskAttemptProps;
import com.miotech.kun.workflow.common.taskrun.bo.TaskRunDailyStatisticInfo;
import com.miotech.kun.workflow.common.taskrun.dao.TaskRunDao;
import com.miotech.kun.workflow.common.taskrun.factory.TaskRunLogVOFactory;
import com.miotech.kun.workflow.common.taskrun.factory.TaskRunStateVOFactory;
import com.miotech.kun.workflow.common.taskrun.filter.TaskRunSearchFilter;
import com.miotech.kun.workflow.common.taskrun.vo.*;
import com.miotech.kun.workflow.core.Executor;
import com.miotech.kun.workflow.core.model.taskrun.TaskAttempt;
import com.miotech.kun.workflow.core.model.taskrun.TaskRun;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunStatus;
import com.miotech.kun.workflow.core.resource.Resource;
import com.miotech.kun.workflow.utils.DateTimeUtils;
import com.miotech.kun.workflow.utils.WorkflowIdGenerator;
import org.apache.dubbo.common.utils.ConcurrentHashSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Singleton
public class TaskRunService {

    private final Logger logger = LoggerFactory.getLogger(TaskRunService.class);

    @Inject
    private TaskRunDao taskRunDao;

    @Inject
    private ResourceLoader resourceLoader;

    @Inject
    private Executor executor;

    private final Set<Long> rerunningTaskRunIds = new ConcurrentHashSet<>();

    @Inject
    public TaskRunService(TaskRunDao taskRunDao,  ResourceLoader resourceLoader, Executor executor) {
        this.taskRunDao = taskRunDao;
        this.resourceLoader = resourceLoader;
        this.executor = executor;
    }

    /* --------------------------------------- */
    /* ----------- public methods ------------ */
    /* --------------------------------------- */

    public Optional<TaskRunVO> getTaskRunDetail(Long taskRunId) {
        Optional<TaskRun> taskRun = taskRunDao.fetchTaskRunById(taskRunId);
        return taskRun.map(this::convertToVO);
    }

    public TaskRun findTaskRun(Long taskRunId) {
        return taskRunDao.fetchTaskRunById(taskRunId)
                .orElseThrow(() -> new EntityNotFoundException("TaskRun with id \"" + taskRunId + "\" not found"));
    }

    public TaskRunStateVO getTaskStatus(Long taskRunId) {
        TaskRun taskRun = findTaskRun(taskRunId);
        return TaskRunStateVOFactory.create(taskRun.getStatus());
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
        if(resource == null){
            List<String> logs = new ArrayList<>();
            return TaskRunLogVOFactory.create(taskRunId, taskAttempt.getAttempt(), startLine, startLine, logs);
        }

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

    public TaskRunDAGVO getNeighbors(Long taskRunId, int upstreamLevel, int downstreamLevel) {
        Preconditions.checkArgument(0 <= upstreamLevel && upstreamLevel <= 5 , "upstreamLevel should be non negative and no greater than 5");
        Preconditions.checkArgument(0 <= downstreamLevel&& downstreamLevel <= 5, "downstreamLevel should be non negative and no greater than 5");

        TaskRun taskRun = findTaskRun(taskRunId);
        List<TaskRun> result = new ArrayList<>();
        result.add(taskRun);
        if (upstreamLevel > 0) {
            result.addAll(getUpstreamTaskRuns(taskRun, upstreamLevel));
        }
        if (downstreamLevel > 0) {
            result.addAll(getDownstreamTaskRuns(taskRun, downstreamLevel));
        }

        List<TaskRunVO> nodes = result.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        List<TaskRunDependencyVO> edges = result.stream()
                .flatMap(x -> x.getDependentTaskRunIds().stream()
                        .map(t -> new TaskRunDependencyVO(x.getId(),t)))
                .collect(Collectors.toList());
        return new TaskRunDAGVO(nodes, edges);
    }

    public List<TaskRun> getUpstreamTaskRuns(TaskRun taskRun, int distance) {
        return taskRunDao.fetchUpstreamTaskRunsById(taskRun.getId(), distance, false);
    }

    public List<TaskRun> getDownstreamTaskRuns(TaskRun taskRun, int distance) {
        return taskRunDao.fetchDownstreamTaskRunsById(taskRun.getId(), distance, false);
    }

    public PaginationVO<TaskRun> searchTaskRuns(TaskRunSearchFilter filter) {
        Preconditions.checkNotNull(filter, "Invalid argument `filter`: null");
        Preconditions.checkNotNull(filter.getPageNum(), "Invalid argument `pageNum`: null");
        Preconditions.checkNotNull(filter.getPageSize(), "Invalid argument `pageSize`: null");

        return PaginationVO.<TaskRun>newBuilder()
                .withPageNumber(filter.getPageNum())
                .withPageSize(filter.getPageSize())
                .withRecords(taskRunDao.fetchTaskRunsByFilter(filter))
                .withTotalCount(taskRunDao.fetchTotalCountByFilter(filter))
                .build();
    }

    public PaginationVO<TaskRunVO> searchTaskRunVOs(TaskRunSearchFilter filter) {
        PaginationVO<TaskRun> runsPage = searchTaskRuns(filter);
        return PaginationVO.<TaskRunVO>newBuilder()
                .withPageNumber(filter.getPageNum())
                .withPageSize(filter.getPageSize())
                .withRecords(runsPage.getRecords().stream().map(this::convertToVO).collect(Collectors.toList()))
                .withTotalCount(runsPage.getTotalCount())
                .build();
    }

    public int countTaskRunVOs(TaskRunSearchFilter filter) {
        Preconditions.checkNotNull(filter, "Invalid argument `filter`: null");
        return taskRunDao.fetchTotalCountByFilter(filter);
    }

    public List<TaskRunDailyStatisticInfo> countTaskRunVOsByDate(TaskRunSearchFilter filter) {
        return countTaskRunVOsByDate(filter, 0);
    }

    public List<TaskRunDailyStatisticInfo> countTaskRunVOsByDate(TaskRunSearchFilter filter, int offsetHours) {
        Preconditions.checkNotNull(filter, "Invalid argument `filter`: null");
        return taskRunDao.fetchTotalCountByDay(filter, offsetHours);
    }

    public TaskRunVO convertToVO(TaskRun taskRun) {
        List<TaskAttemptProps> attempts = taskRunDao.fetchAttemptsPropByTaskRunId(taskRun.getId())
                .stream()
                // Part of id properties are missing after fetched from storage
                .map(attempt -> attempt.cloneBuilder()
                        .withTaskRunId(taskRun.getId())
                        .withTaskId(taskRun.getTask().getId())
                        .withTaskName(taskRun.getTask().getName())
                        .build())
                .collect(Collectors.toList());

        TaskRunVO vo = new TaskRunVO();
        vo.setTask(taskRun.getTask());
        vo.setId(taskRun.getId());
        vo.setScheduledTick(taskRun.getScheduledTick());
        vo.setStatus(taskRun.getStatus());
        vo.setInlets(taskRun.getInlets());
        vo.setOutlets(taskRun.getOutlets());
        vo.setDependentTaskRunIds(taskRun.getDependentTaskRunIds());
        vo.setStartAt(taskRun.getStartAt());
        vo.setEndAt(taskRun.getEndAt());
        vo.setCreatedAt(taskRun.getCreatedAt());
        vo.setUpdatedAt(taskRun.getUpdatedAt());
        vo.setConfig(taskRun.getConfig());
        vo.setAttempts(attempts);
        return vo;
    }

    /**
     * Re-run a taskrun instance. Any currently unfinished task attempts shall be aborted.
     * @param taskRunId id of target taskrun
     * @return <code>true</code> if success, <code>false</code> if failed to rerun.
     * @throws IllegalStateException when cannot find latest task attempt corresponding to task run
     */
    public boolean rerunTaskRun(Long taskRunId) {
        // 1. Preconditions check
        Preconditions.checkArgument(Objects.nonNull(taskRunId), "Argument `taskRunId` should not be null");
        Optional<TaskRun> taskRunOptional = taskRunDao.fetchTaskRunById(taskRunId);
        logger.info("Trying to re-run taskrun instance with id = {}.", taskRunId);
        if (!taskRunOptional.isPresent()) {
            logger.warn("Cannot rerun taskrun instance with id = {}. Reason: task run does not exists.", taskRunId);
            return false;
        }
        TaskRun taskRun = taskRunOptional.get();
        // Does the same re-run request invoked in another threads?
        if (!rerunningTaskRunIds.add(taskRunId)) {
            return false;
        }

        try {
            // 2. check if latest task attempt is finished.
            // If it is still running, we shall not create another attempt before it is finished.
            TaskAttemptProps latestAttempt = taskRunDao.fetchLatestTaskAttempt(taskRunId);
            if (!latestAttemptIsFinished(latestAttempt, taskRunId)) {
                return false;
            }
            // 3. Submit a new attempt to executor
            return submitReRunToExecutor(taskRun, latestAttempt);
        } catch (Exception e) {
            logger.error("Failed to re-run taskrun with id = {} due to exceptions.", taskRunId);
            throw e;
        } finally {
            // release the lock
            rerunningTaskRunIds.remove(taskRunId);
        }
    }

    private boolean latestAttemptIsFinished(TaskAttemptProps latestAttempt, Long taskRunId) {
        if (Objects.isNull(latestAttempt)) {
            throw new IllegalStateException(
                    String.format("Unexpected state: cannot find any attempt for task run with id = %s when trying to rerun.", taskRunId)
            );
        }
        if (!latestAttempt.getStatus().isFinished()) {
            logger.info("Cannot rerun taskrun instance with id = {}. Reason: latest attempt (id = {}) is still running.", taskRunId, latestAttempt.getId());
            return false;
        }
        return true;
    }

    private boolean submitReRunToExecutor(TaskRun taskRun, TaskAttemptProps latestAttempt) {
        Preconditions.checkNotNull(taskRun);
        // TODO: @yide 不应该在TaskManager以外的地方直接构造TaskAttempt并提交给Executor，否则依赖等会有问题，
        // 因为依赖的管理目前是由TaskManager完成的。
        TaskAttempt newAttempt = TaskAttempt.newBuilder()
                .withId(WorkflowIdGenerator.nextTaskAttemptId(taskRun.getId(), latestAttempt.getAttempt() + 1))
                .withTaskRun(taskRun)
                .withAttempt(latestAttempt.getAttempt() + 1)
                .withStatus(TaskRunStatus.CREATED)
                .build();
        taskRunDao.createAttempt(newAttempt);
        return executor.submit(newAttempt);
    }

    public boolean abortTaskRun(Long taskRunId) {
        TaskAttemptProps attempt = taskRunDao.fetchLatestTaskAttempt(taskRunId);

        if (Objects.isNull(attempt)) {
            throw new IllegalArgumentException("Attempt is not found for taskRunId: " + taskRunId);
        }

        return executor.cancel(attempt.getId());
    }

    public String logPathOfTaskAttempt(Long taskAttemptId) {
        String date = DateTimeUtils.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return String.format("file:logs/%s/%s", date, taskAttemptId);
    }

    public Map<Long, List<TaskRunVO>> fetchLatestTaskRuns(List<Long> taskIds, int limit) {
        Preconditions.checkNotNull(taskIds);
        Preconditions.checkArgument(limit > 0);
        Preconditions.checkArgument(limit <= 100);

        Map<Long, List<TaskRunVO>> mappings = new HashMap<>();
        for (Long taskId : taskIds) {
            List<TaskRun> latestTaskRuns = taskRunDao.fetchLatestTaskRuns(taskId, limit);
            mappings.put(taskId, latestTaskRuns.stream().map(this::convertToVO).collect(Collectors.toList()));
        }
        return mappings;
    }
}
