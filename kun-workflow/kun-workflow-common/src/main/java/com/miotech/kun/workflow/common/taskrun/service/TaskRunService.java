package com.miotech.kun.workflow.common.taskrun.service;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.miotech.kun.commons.utils.ExceptionUtils;
import com.miotech.kun.commons.utils.Props;
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
import com.miotech.kun.workflow.core.Scheduler;
import com.miotech.kun.workflow.core.annotation.Internal;
import com.miotech.kun.workflow.core.model.task.TaskPriority;
import com.miotech.kun.workflow.core.model.taskrun.TaskRun;
import com.miotech.kun.workflow.core.resource.Resource;
import com.miotech.kun.workflow.utils.DateTimeUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.dubbo.common.utils.ConcurrentHashSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
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

    @Inject
    private Scheduler scheduler;

    @Inject
    private Props props;

    private final Set<Long> rerunningTaskRunIds = new ConcurrentHashSet<>();

    @Inject
    public TaskRunService(TaskRunDao taskRunDao, ResourceLoader resourceLoader, Executor executor, Scheduler scheduler) {
        this.taskRunDao = taskRunDao;
        this.resourceLoader = resourceLoader;
        this.executor = executor;
        this.scheduler = scheduler;
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

    /**
     * Fetch output log of a task run attempt instance.
     *
     * @param taskRunId      id of target task run
     * @param attempt        attempt number of that task run
     * @param startLineIndex the starting line number to read.
     *                       When set to null will automatically set to 0.
     *                       When set to negative long value, will use negative indexes of lines.
     *                       For instance, a file with 7 lines:
     *                       [L1, L2, L3, L4, L5, L6, L7]
     *                       indexes:  [ 0,  1,  2,  3,  4,  5,  6]
     *                       negative indexes:  [-7, -6, -5, -4, -3, -2, -1]
     *                       A usage example is that, if you want to read the last 5000 lines of log,
     *                       then set startLine = -5000 and endLine = null.
     * @param endLineIndex   The final line that stops *before*. (For instance, startLine = 2, endLine = 5
     *                       will read line with index 2, 3, 4 (line 0, 1 and the lines after L4 will be ignored)
     *                       When set to null, will goes to the end of file automatically.
     *                       When set to negative, will use negative indexes like startLine does.
     * @return task run log value object
     */
    public TaskRunLogVO getTaskRunLog(final Long taskRunId,
                                      final int attempt,
                                      final Integer startLineIndex,
                                      final Integer endLineIndex) {
        Optional<TaskAttemptProps> taskAttemptPropsOptional = findTaskAttemptProps(taskRunId, attempt);
        if (!taskAttemptPropsOptional.isPresent()) {
            logger.warn("Cannot find task attempt {} of task run with id = {}.", attempt, taskRunId);
            throw new EntityNotFoundException(String.format("Cannot find task attempt %s of task run with id = %s.", attempt, taskRunId));
        }

        TaskAttemptProps taskAttempt = taskAttemptPropsOptional.get();
        Resource resource;
        int lineCount = 0;
        if (endLineIndex == Integer.MAX_VALUE) {
            try {
                logger.debug("trying to get worker log from executor");
                String logs = executor.workerLog(taskAttempt.getId(),0 - startLineIndex);
                List<String> logList = coverLogsToList(logs);
                logger.debug("get logs from executor success,line count = {}", lineCount);
                return TaskRunLogVOFactory.create(taskRunId, taskAttempt.getAttempt(), startLineIndex, endLineIndex, logList);
            } catch (RuntimeException e) {
                logger.warn("get taskAttemptId = {} from executor failed", taskAttempt.getId(), e);
            }
        }
        try {
            resource = resourceLoader.getResource(taskAttempt.getLogPath());
            lineCount = getLineCountOfFile(resource);

        } catch (RuntimeException e) {
            logger.warn("Cannot find or open log path for existing task attempt: {}", taskAttempt.getLogPath());
            return TaskRunLogVOFactory.createLogNotFound(taskRunId, taskAttempt.getAttempt());
        }

        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(resource.getInputStream()))) {
            Triple<List<String>, Integer, Integer> result = readLinesFromLogFile(bufferedReader, lineCount, startLineIndex, endLineIndex);
            return TaskRunLogVOFactory.create(taskRunId, taskAttempt.getAttempt(), result.getMiddle(), result.getRight(), result.getLeft());
        } catch (IOException e) {
            logger.error("Failed to get task attempt log: {}", taskAttempt.getLogPath(), e);
            throw ExceptionUtils.wrapIfChecked(e);
        }
    }

    private List<String> coverLogsToList(String logs) {
        return Arrays.stream(logs.split("\n")).collect(Collectors.toList());
    }

    private Optional<TaskAttemptProps> findTaskAttemptProps(long taskRunId, int attempt) {
        List<TaskAttemptProps> attempts = taskRunDao.fetchAttemptsPropByTaskRunId(taskRunId);
        TaskAttemptProps taskAttempt;
        if (attempts.isEmpty()) {
            return Optional.empty();
        }
        attempts.sort((o1, o2) -> o1.getAttempt() < o2.getAttempt() ? 1 : -1);

        if (attempt > 0) {
            taskAttempt = attempts.stream()
                    .filter(x -> x.getAttempt() == attempt)
                    .findFirst()
                    .orElse(null);
        } else {
            taskAttempt = attempts.get(0);
        }

        return Optional.ofNullable(taskAttempt);
    }

    /**
     * Read lines of a log file in specific range.
     *
     * @param reader         buffered reader instance
     * @param totalLineCount total line count of that log file
     * @param startLineIndex Index of start line. Allows negative indexes.
     * @param endLineIndex   Index of stop line. Allows negative indexes.
     * @return Triple of (log lines, actual start line index, actual end line index)
     * @throws IOException if log file not found, or other IO exception cases
     */
    @Internal
    public Triple<List<String>, Integer, Integer> readLinesFromLogFile(BufferedReader reader, int totalLineCount, Integer startLineIndex, Integer endLineIndex) throws IOException {
        int startLineActual = (startLineIndex == null) ? 0 : ((startLineIndex >= 0) ? startLineIndex : totalLineCount + startLineIndex);
        int endLineActual = (endLineIndex == null) ? Integer.MAX_VALUE : ((endLineIndex >= 0) ? endLineIndex : totalLineCount + endLineIndex);

        String line;
        List<String> logs = new ArrayList<>();

        int i = 0;
        for (; (i < endLineActual) && (line = reader.readLine()) != null; i++) {
            if (i >= startLineActual) {
                logs.add(line);
            }
        }
        // Triple of (logs, actual start line index, actual end line index)
        return Triple.of(logs, startLineActual, startLineActual + logs.size() - 1);
    }

    /**
     * Get line count of a resource
     *
     * @param fileResource resource instance
     * @return An non-negative integer of total file line count.
     */
    @Internal
    private int getLineCountOfFile(Resource fileResource) {
        int lineCount;
        try (LineNumberReader lineNumberReader = new LineNumberReader(new InputStreamReader(fileResource.getInputStream()))) {
            while (null != lineNumberReader.readLine()) ;  // loop until EOF
            lineCount = lineNumberReader.getLineNumber();
        } catch (IOException e) {
            logger.error("Failed to create line number reader for resource: {}", fileResource);
            throw ExceptionUtils.wrapIfChecked(e);
        }
        return lineCount;
    }

    public TaskRunDAGVO getNeighbors(Long taskRunId, int upstreamLevel, int downstreamLevel) {
        Preconditions.checkArgument(0 <= upstreamLevel && upstreamLevel <= 5, "upstreamLevel should be non negative and no greater than 5");
        Preconditions.checkArgument(0 <= downstreamLevel && downstreamLevel <= 5, "downstreamLevel should be non negative and no greater than 5");

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
                        .map(t -> new TaskRunDependencyVO(x.getId(), t)))
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
                .withRecords(convertToVO(runsPage.getRecords()))
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

    public List<TaskRunVO> convertToVO(List<TaskRun> taskRuns) {
        List<Long> taskRunIds = taskRuns.stream().map(TaskRun::getId).collect(Collectors.toList());
        List<TaskAttemptProps> taskAttemptProps = taskRunDao.fetchAttemptsPropByTaskRunIds(taskRunIds);
        Map<Long, List<TaskAttemptProps>> taskAttemptPropsMap = groupByTaskRunId(taskAttemptProps);

        return taskRuns.stream().map(taskRun ->
                buildTaskRunVO(taskRun, taskAttemptPropsMap.get(taskRun.getId()))).collect(Collectors.toList());
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
        return buildTaskRunVO(taskRun, attempts);
    }

    /**
     * Re-run a taskrun instance. Any currently unfinished task attempts shall be aborted.
     *
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
        return scheduler.rerun(taskRun);


    }

    public boolean abortTaskRun(Long taskRunId) {
        TaskAttemptProps attempt = taskRunDao.fetchLatestTaskAttempt(taskRunId);

        if (Objects.isNull(attempt)) {
            throw new IllegalArgumentException("Attempt is not found for taskRunId: " + taskRunId);
        }

        return executor.cancel(attempt.getId());
    }

    public String logPathOfTaskAttempt(Long taskAttemptId) {
        String logDir = props.getString("resource.logDirectory", "logs");
        String date = DateTimeUtils.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return String.format("file:%s/%s/%s", logDir, date, taskAttemptId);
    }

    public boolean updateTaskAttemptLogPath(Long taskAttemptId, String logPath) {
        return taskRunDao.updateTaskAttemptLogPath(taskAttemptId, logPath);
    }

    public Map<Long, List<TaskRunVO>> fetchLatestTaskRuns(List<Long> taskIds, int limit) {
        Preconditions.checkNotNull(taskIds);
        Preconditions.checkArgument(limit > 0);
        Preconditions.checkArgument(limit <= 100);

        Map<Long, List<TaskRun>> taskIdToLatestTaskRunsMap = taskRunDao.fetchLatestTaskRunsByBatch(taskIds, limit);

        Map<Long, List<TaskRunVO>> mappings = new HashMap<>();
        for (Map.Entry<Long, List<TaskRun>> entry : taskIdToLatestTaskRunsMap.entrySet()) {
            List<TaskRun> runs = entry.getValue();
            mappings.put(entry.getKey(), runs.stream().map(this::convertToVO).collect(Collectors.toList()));
        }
        return mappings;
    }

    public boolean changeTaskAttemptPriority(long taskRunId, int priority) {
        Optional<TaskRun> taskRunOptional = taskRunDao.fetchTaskRunById(taskRunId);
        if (!taskRunOptional.isPresent()) {
            throw new IllegalArgumentException("taskRun is not found for taskRunId: " + taskRunId);
        }
        TaskAttemptProps attempt = taskRunDao.fetchLatestTaskAttempt(taskRunId);
        if (Objects.isNull(attempt)) {
            throw new IllegalArgumentException("Attempt is not found for taskRunId: " + taskRunId);
        }
        executor.changePriority(attempt.getId(), attempt.getQueueName(), TaskPriority.resolvePriority(priority));
        taskRunDao.updateTaskRun(taskRunOptional.get().
                cloneBuilder().withPriority(priority).build());
        return true;
    }

    private Map<Long, List<TaskAttemptProps>> groupByTaskRunId(List<TaskAttemptProps> taskAttemptProps) {
        Map<Long, List<TaskAttemptProps>> taskAttemptPropsMap = Maps.newHashMap();
        for (TaskAttemptProps taskAttemptProp : taskAttemptProps) {
            if (taskAttemptPropsMap.containsKey(taskAttemptProp.getTaskRunId())) {
                taskAttemptPropsMap.get(taskAttemptProp.getTaskRunId()).add(taskAttemptProp);
                continue;
            }

            List<TaskAttemptProps> taskAttemptPropsList = Lists.newArrayList(taskAttemptProp);
            taskAttemptPropsMap.put(taskAttemptProp.getTaskRunId(), taskAttemptPropsList);
        }
        return taskAttemptPropsMap;
    }

    private TaskRunVO buildTaskRunVO(TaskRun taskRun, List<TaskAttemptProps> attempts) {
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

}
