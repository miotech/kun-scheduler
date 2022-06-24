package com.miotech.kun.workflow.common.taskrun.service;

import com.google.common.collect.Lists;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.miotech.kun.commons.utils.Props;
import com.miotech.kun.workflow.LocalScheduler;
import com.miotech.kun.workflow.TaskRunStateMachine;
import com.miotech.kun.workflow.common.CommonTestBase;
import com.miotech.kun.workflow.common.exception.EntityNotFoundException;
import com.miotech.kun.workflow.common.lineage.service.LineageService;
import com.miotech.kun.workflow.common.resource.ResourceLoader;
import com.miotech.kun.workflow.common.task.dao.TaskDao;
import com.miotech.kun.workflow.common.taskrun.bo.TaskAttemptProps;
import com.miotech.kun.workflow.common.taskrun.dao.TaskRunDao;
import com.miotech.kun.workflow.common.taskrun.vo.TaskRunGanttChartVO;
import com.miotech.kun.workflow.common.taskrun.vo.TaskRunLogVO;
import com.miotech.kun.workflow.common.taskrun.vo.TaskRunVO;
import com.miotech.kun.workflow.core.Executor;
import com.miotech.kun.workflow.core.event.TaskAttemptStatusChangeEvent;
import com.miotech.kun.workflow.core.execution.Config;
import com.miotech.kun.workflow.core.model.common.Tick;
import com.miotech.kun.workflow.core.model.task.ScheduleConf;
import com.miotech.kun.workflow.core.model.task.ScheduleType;
import com.miotech.kun.workflow.core.model.task.Task;
import com.miotech.kun.workflow.core.model.taskrun.TaskAttempt;
import com.miotech.kun.workflow.core.model.taskrun.TaskRun;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunStatus;
import com.miotech.kun.workflow.core.model.taskrun.TimeType;
import com.miotech.kun.workflow.core.resource.Resource;
import com.miotech.kun.workflow.testing.factory.MockTaskAttemptFactory;
import com.miotech.kun.workflow.testing.factory.MockTaskFactory;
import com.miotech.kun.workflow.testing.factory.MockTaskRunFactory;
import com.miotech.kun.workflow.utils.DateTimeUtils;
import com.miotech.kun.workflow.utils.WorkflowIdGenerator;
import org.apache.commons.lang3.tuple.Triple;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Ignore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import java.io.*;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.*;
import static com.shazam.shazamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class TaskRunServiceTest extends CommonTestBase {
    private TaskRunService taskRunService;

    @Inject
    private TaskDao taskDao;

    @Inject
    private TaskRunDao taskRunDao;

    @Inject
    private ResourceLoader resourceLoader;

    private final Executor executor = mock(Executor.class);

    @Inject
    private LocalScheduler scheduler;

    @Inject
    private EventBus eventBus;

    @Inject
    private Props props;

    private TaskRunStateMachine taskRunStateMachine;

    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Override
    protected void configuration() {
        super.configuration();
        bind(Executor.class,mock(Executor.class));
    }


    @BeforeEach
    public void setUp() throws IOException {
        tempFolder.create();
        this.taskRunDao = spy(this.taskRunDao);
        this.taskRunService = spy(new TaskRunService(
                taskRunDao,
                executor,
                scheduler,
                eventBus,
                props
        ));
        taskRunStateMachine = new TaskRunStateMachine(taskRunDao, eventBus, mock(LineageService.class));
        taskRunStateMachine.start();
    }

    @Test
    public void testGetTaskRunDetail() {
        TaskRun taskRun = prepareData();
        TaskRunVO existedRun = taskRunService.getTaskRunDetail(taskRun.getId()).get();
        assertNotNull(existedRun);
        assertNotNull(existedRun.getTask());
    }


    private TaskRun prepareData() {
        long testId = 1L;

        Task task = MockTaskFactory.createTask().cloneBuilder().withId(22L)
                .withName("test task")
                .withDescription("")
                .withOperatorId(1L)
                .withScheduleConf(new ScheduleConf(ScheduleType.NONE, null))
                .withConfig(Config.EMPTY)
                .withDependencies(new ArrayList<>())
                .withTags(new ArrayList<>())
                .build();
        taskDao.create(task);

        TaskRun taskRun = TaskRun.newBuilder()
                .withId(testId)
                .withTask(task)
                .withStatus(TaskRunStatus.CREATED)
                .withConfig(Config.EMPTY)
                .withScheduleType(task.getScheduleConf().getType())
                .withDependentTaskRunIds(Collections.emptyList())
                .withInlets(Collections.emptyList())
                .withOutlets(Collections.emptyList())
                .withScheduledTick(new Tick(""))
                .withScheduleTime(new Tick(""))
                .build();
        taskRunDao.createTaskRun(taskRun);
        return taskRun;
    }

    //get log logic has moved into executor
    @Ignore
    public void getTaskRunLog() throws IOException {
        Long taskRunId = 1L;
        String testStr = "hellow world";
        Resource resource = createResource("xyz", testStr, 1);

        TaskAttemptProps attemptProps1 = TaskAttemptProps.newBuilder()
                .withAttempt(1)
                .withLogPath(resource.getLocation())
                .build();
        TaskAttemptProps attemptProps2 = TaskAttemptProps.newBuilder()
                .withAttempt(2)
                .withLogPath(resource.getLocation())
                .build();

        List<TaskAttemptProps> attempts = new ArrayList<>();
        attempts.add(attemptProps1);
        attempts.add(attemptProps2);

        Mockito.when(taskRunDao.fetchAttemptsPropByTaskRunId(taskRunId))
                .thenReturn(attempts);

        TaskRunLogVO taskRunLogVO;

        // attempt = 1
        taskRunLogVO = taskRunService.getTaskRunLog(taskRunId, 1, 0, 3);
        assertEquals(1, taskRunLogVO.getLogs().size());
        assertEquals(1, taskRunLogVO.getAttempt());

        // attempt = 2
        taskRunLogVO = taskRunService.getTaskRunLog(taskRunId, 2, 0, 3);
        assertEquals(1, taskRunLogVO.getLogs().size());
        assertEquals(2, taskRunLogVO.getAttempt());

        // attempt = -1 as latest
        taskRunLogVO = taskRunService.getTaskRunLog(taskRunId, -1, 0, 3);
        assertEquals(1, taskRunLogVO.getLogs().size());
        assertEquals(2, taskRunLogVO.getAttempt());
        assertEquals(testStr, taskRunLogVO.getLogs().get(0));

    }

    //get log logic has moved into executor
    @Ignore
    public void getTaskRunLog_withstartLine() throws IOException {
        Long taskRunId = 1L;
        Resource resource = createResource("xyz", "hello world\n", 3);

        TaskAttemptProps attemptProps = TaskAttemptProps.newBuilder()
                .withAttempt(1)
                .withLogPath(resource.getLocation())
                .build();

        List<TaskAttemptProps> attempts = new ArrayList<>();
        attempts.add(attemptProps);

        Mockito.when(taskRunDao.fetchAttemptsPropByTaskRunId(taskRunId))
                .thenReturn(attempts);

        TaskRunLogVO taskRunLogVO;

        taskRunLogVO = taskRunService.getTaskRunLog(taskRunId, 1, 0, 3);
        assertEquals(3, taskRunLogVO.getLogs().size());

        taskRunLogVO = taskRunService.getTaskRunLog(taskRunId, 1, 2, 3);
        assertEquals(1, taskRunLogVO.getLogs().size());

        taskRunLogVO = taskRunService.getTaskRunLog(taskRunId, 1, 3, 3);
        assertEquals(0, taskRunLogVO.getLogs().size());
    }

    //get log logic has moved into executor
    @Ignore
    public void getTaskRunLog_withendLine() throws IOException {
        Long taskRunId = 1L;
        Resource resource = createResource("xyz", "hello world\n", 3);


        TaskAttemptProps attemptProps = TaskAttemptProps.newBuilder()
                .withAttempt(1)
                .withLogPath(resource.getLocation())
                .build();

        List<TaskAttemptProps> attempts = new ArrayList<>();
        attempts.add(attemptProps);

        Mockito.when(taskRunDao.fetchAttemptsPropByTaskRunId(taskRunId))
                .thenReturn(attempts);

        TaskRunLogVO taskRunLogVO;

        taskRunLogVO = taskRunService.getTaskRunLog(taskRunId, 1, 0, 1);
        // should return line with index 0 (line 1)
        assertEquals(1, taskRunLogVO.getLogs().size());

        // should return line with index 0, 1, 2, 3 (line 1, 2, 3, 4)
        taskRunLogVO = taskRunService.getTaskRunLog(taskRunId, 1, 0, 4);
        assertEquals(3, taskRunLogVO.getLogs().size());
    }

    @Test
    public void getTaskRunLog_withNotExistingTaskRun_shouldThrowException() throws IOException {
        Long taskRunId = 1L;

        Mockito.when(taskRunDao.fetchAttemptsPropByTaskRunId(taskRunId))
                .thenReturn(Collections.emptyList());
        try {
            taskRunService.getTaskRunLog(taskRunId, -1, 1, 2);
        } catch (Exception e) {
            assertEquals(EntityNotFoundException.class, e.getClass());
            assertEquals("Cannot find task attempt -1 of task run with id = 1.", e.getMessage());
        }
    }

    //get log logic has moved into executor
    @Ignore
    public void getTaskRunLog_shouldAllowSearchByNegativeIndex() throws IOException {
        // Prepare
        Long taskRunId = 1L;
        String testStr = "line 1\nline 2\nline 3\nline 4\n";
        Resource resource = createResource("xyz", testStr, 1);
        TaskAttemptProps attemptProps = TaskAttemptProps.newBuilder()
                .withAttempt(1)
                .withLogPath(resource.getLocation())
                .build();

        Mockito.when(taskRunDao.fetchAttemptsPropByTaskRunId(taskRunId))
                .thenReturn(Collections.singletonList(attemptProps));

        TaskRunLogVO vo = taskRunService.getTaskRunLog(taskRunId, -1, -3, null);
        assertThat(vo.getLogs().size(), is(3));
        assertThat(vo.getLogs().get(0), is("line 2"));
        assertThat(vo.getAttempt(), is(1));
        assertThat(vo.getStartLine(), is(1));
        assertThat(vo.getEndLine(), is(3));
    }

    @Test
    public void abortTaskRun_shouldSendAbortSignalsToExecutors() {
        // Prepare
        Mockito.doAnswer(invocation -> {
            TaskAttemptProps attemptPropsTemplate = TaskAttemptProps.newBuilder()
                    .withStatus(TaskRunStatus.RUNNING)
                    .build();
            return attemptPropsTemplate.cloneBuilder().withId(11L).build();
        }).when(taskRunDao).fetchLatestTaskAttempt(Mockito.anyLong());

        Map<Long, Boolean> stopSignalReceived = new HashMap<>();
        Mockito.doAnswer(invocation -> {
            Long id = invocation.getArgument(0);
            stopSignalReceived.put(id, true);
            return true;
        }).when(executor).cancel(Mockito.anyLong());

        // process
        boolean result = taskRunService.abortTaskRun(1L);

        // Validate
        assertThat(result, is(true));
        assertThat(stopSignalReceived.getOrDefault(11L, false), is(true));
    }

    private Resource createResource(String fileName, String text, int times) throws IOException {
        File file = tempFolder.newFile(fileName);
        Resource resource = resourceLoader.getResource("file://" + file.getPath(), true);
        Writer writer = new PrintWriter(resource.getOutputStream());

        for (int i = 0; i < times; i++) {
            writer.write(text);
        }
        writer.flush();
        return resource;
    }

    @Test
    public void fetchLatestTaskRuns_shouldFetchLatestTaskRunsAndReturnsMappedResults() {
        // Prepare
        prepareDataForFetchLatestTaskRuns();
        long task1Id = 22L;
        long task2Id = 33L;
        long task3Id = 44L;

        // Process
        List<Long> taskIdsToQuery = Lists.newArrayList(task1Id, task2Id, task3Id);
        Map<Long, List<TaskRunVO>> mappings = taskRunService.fetchLatestTaskRuns(taskIdsToQuery, 10, false);

        // Validate
        assertThat(mappings.size(), is(3));
        // task1 has 20 runs, but size of fetched result should be limited to 10
        assertThat(mappings.get(task1Id).size(), is(10));
        assertThat(mappings.get(task2Id).size(), is(5));
        assertThat(mappings.get(task3Id).size(), is(0));
    }


    @Test
    public void fetchLatestTaskRunsOrderByCreateTime() {
        prepareTaskRuns();
        List<Long> taskIdsToQuery = Lists.newArrayList(22L);
        Map<Long, List<TaskRunVO>> mappings = taskRunService.fetchLatestTaskRuns(taskIdsToQuery, 10,false);
        List<TaskRunVO> taskRunVOS = mappings.get(22L);
        assertThat(taskRunVOS, hasSize(2));
        assertThat(taskRunVOS.get(0).getId(), is(2L));
        assertThat(taskRunVOS.get(1).getId(), is(1L));
    }

    @Test
    public void changeTaskRunPriority() {

        //prepare
        Task task = MockTaskFactory.createTask();
        TaskRun taskRun = MockTaskRunFactory.createTaskRun(task);
        TaskAttempt taskAttempt = MockTaskAttemptFactory.createTaskAttempt(taskRun);
        taskDao.create(task);
        taskRunDao.createTaskRun(taskRun);
        taskRunDao.createAttempt(taskAttempt);

        taskRunService.changeTaskRunPriority(taskRun.getId(), 32);

        //verify
        TaskRun updatedTaskRun = taskRunDao.fetchTaskRunById(taskRun.getId()).get();
        TaskAttempt updatedAttempt = taskRunDao.fetchAttemptById(taskAttempt.getId()).get();
        assertThat(updatedTaskRun.getTask().getPriority(), is(0));
        assertThat(updatedTaskRun.getPriority(), is(32));
        assertThat(updatedAttempt.getPriority(), is(32));
    }

    @Test
    public void changeTaskRunPriorityHasInQueued_should_invoke_executor() {

        //prepare
        Task task = MockTaskFactory.createTask();
        TaskRun taskRun = MockTaskRunFactory.createTaskRun(task);
        TaskAttempt taskAttempt = MockTaskAttemptFactory.createTaskAttempt(taskRun)
                .cloneBuilder()
                .withStatus(TaskRunStatus.QUEUED)
                .build();
        taskDao.create(task);
        taskRunDao.createTaskRun(taskRun);
        taskRunDao.createAttempt(taskAttempt);

        taskRunService.changeTaskRunPriority(taskRun.getId(), 32);

        //verify
        TaskRun updatedTaskRun = taskRunDao.fetchTaskRunById(taskRun.getId()).get();
        TaskAttempt updatedAttempt = taskRunDao.fetchAttemptById(taskAttempt.getId()).get();
        assertThat(updatedTaskRun.getTask().getPriority(), is(0));
        assertThat(updatedTaskRun.getPriority(), is(32));
        assertThat(updatedAttempt.getPriority(), is(32));

        verify(executor, times(1)).changePriority(taskAttempt.getId(), taskAttempt.getQueueName(), 32);
    }


    private void prepareTaskRuns() {
        Task task1 = MockTaskFactory.createTask().cloneBuilder().withId(22L)
                .withName("test task 1")
                .withDescription("")
                .withOperatorId(1L)
                .withScheduleConf(new ScheduleConf(ScheduleType.NONE, null))
                .withConfig(Config.EMPTY)
                .withDependencies(new ArrayList<>())
                .withTags(new ArrayList<>())
                .build();

        taskDao.create(task1);
        TaskRun taskRun1 = TaskRun.newBuilder()
                .withId(1L)
                .withTask(task1)
                .withStatus(TaskRunStatus.RUNNING)
                .withConfig(Config.EMPTY)
                .withScheduleType(task1.getScheduleConf().getType())
                .withDependentTaskRunIds(Collections.emptyList())
                .withInlets(Collections.emptyList())
                .withOutlets(Collections.emptyList())
                .withScheduledTick(new Tick(DateTimeUtils.now()))
                .withScheduleTime(new Tick(DateTimeUtils.now()))
                .build();
        taskRunDao.createTaskRun(taskRun1);
        try {
            Thread.sleep(1000l);
        } catch (Exception e) {

        }


        TaskRun taskRun2 = TaskRun.newBuilder()
                .withId(2L)
                .withTask(task1)
                .withStatus(TaskRunStatus.RUNNING)
                .withConfig(Config.EMPTY)
                .withScheduleType(task1.getScheduleConf().getType())
                .withDependentTaskRunIds(Collections.emptyList())
                .withInlets(Collections.emptyList())
                .withOutlets(Collections.emptyList())
                .withScheduledTick(new Tick(DateTimeUtils.now().plusMinutes(-1)))
                .withScheduleTime(new Tick(DateTimeUtils.now().plusMinutes(-1)))
                .withStartAt(DateTimeUtils.now().plusMinutes(-1))
                .build();
        taskRunDao.createTaskRun(taskRun2);
    }

    private void prepareDataForFetchLatestTaskRuns() {
        Task task1 = MockTaskFactory.createTask().cloneBuilder().withId(22L)
                .withName("test task 1")
                .withDescription("")
                .withOperatorId(1L)
                .withScheduleConf(new ScheduleConf(ScheduleType.NONE, null))
                .withConfig(Config.EMPTY)
                .withDependencies(new ArrayList<>())
                .withTags(new ArrayList<>())
                .build();
        Task task2 = task1.cloneBuilder().withId(33L).withName("test task 2").build();

        taskDao.create(task1);
        taskDao.create(task2);

        // insert 20 runs for task 1
        for (int i = 1; i <= 20; i++) {
            TaskRun taskRun = TaskRun.newBuilder()
                    .withId(100L + i)
                    .withTask(task1)
                    .withStatus(TaskRunStatus.RUNNING)
                    .withConfig(Config.EMPTY)
                    .withScheduleType(task1.getScheduleConf().getType())
                    .withDependentTaskRunIds(Collections.emptyList())
                    .withInlets(Collections.emptyList())
                    .withOutlets(Collections.emptyList())
                    .withScheduledTick(new Tick(""))
                    .withScheduleTime(new Tick(""))
                    .build();
            taskRunDao.createTaskRun(taskRun);
        }
        // insert 5 runs for task 2
        for (int i = 1; i <= 5; i++) {
            TaskRun taskRun = TaskRun.newBuilder()
                    .withId(120L + i)
                    .withTask(task2)
                    .withStatus(TaskRunStatus.RUNNING)
                    .withConfig(Config.EMPTY)
                    .withScheduleType(task2.getScheduleConf().getType())
                    .withDependentTaskRunIds(Collections.emptyList())
                    .withInlets(Collections.emptyList())
                    .withOutlets(Collections.emptyList())
                    .withScheduledTick(new Tick(""))
                    .withScheduleTime(new Tick(""))
                    .build();
            taskRunDao.createTaskRun(taskRun);
        }
    }

    @Test
    public void rerunTaskRun_whenLatestAttemptIsFinished_shouldSubmitNewAttemptToExecutor() {
        // 1. Prepare
        TaskRun taskRunFailed = prepareTaskRunAndTaskAttempt(TaskRunStatus.FAILED);
        mockExecutorOnSubmit();

        // 2. Process
        boolean rerunSuccessfully = taskRunService.rerunTaskRun(taskRunFailed.getId());

        // 3. Validate
        assertTrue(rerunSuccessfully);
    }

    @Test
    public void rerunTaskRun_whenLatestAttemptNotFinished_shouldThrowsIllegalStateException() throws IllegalStateException {
        // Prepare
        TaskRun taskRunFailed = prepareTaskRunAndTaskAttempt(TaskRunStatus.RUNNING);
        mockExecutorOnSubmit();

        // Process
        Exception ex = assertThrows(IllegalStateException.class, () -> taskRunService.rerunTaskRun(taskRunFailed.getId()));
        assertEquals("taskRun status must be allowed to retry", ex.getMessage());
    }

    @Test
    public void rerunTaskRun_whenTaskRunDoesNotExist_shouldProduceNoEffect() {
        // 1. Prepare
        mockExecutorOnSubmit();

        // 2. Process
        boolean rerunSuccessfully = taskRunService.rerunTaskRun(1234567L);

        // 3. Validate
        assertFalse(rerunSuccessfully);
    }

    @Test
    public void rerunTaskRun_whenLatestAttemptNotFound_shouldThrowIllegalStateException() {
        // 1. Prepare a task run but does not create any task run
        TaskRun preparedTaskRunWithoutAttempt = prepareData();

        try {
            taskRunService.rerunTaskRun(preparedTaskRunWithoutAttempt.getId());
            fail();
        } catch (Exception e) {
            assertThat(e, instanceOf(IllegalStateException.class));
        }
    }

    @Disabled
    @Test
    public void rerunTaskRun_whenConcurrentlyInvoked_shouldOnlyTakeOneEffect() throws Exception {
        // 1. Prepare
        TaskRun taskRunFailed = prepareTaskRunAndTaskAttempt(TaskRunStatus.FAILED);
        mockExecutorOnSubmit();

        List<RunnableFuture> futures = new ArrayList<>();
        for (int i = 0; i < 5; ++i) {
            futures.add(new FutureTask(() -> {
                return taskRunService.rerunTaskRun(taskRunFailed.getId());
            }));
        }

        // 2. Process
        ExecutorService es = Executors.newCachedThreadPool();
        futures.forEach(future -> {
            es.execute(future);
        });
        int successCount = 0;
        for (int i = 0; i < 5; ++i) {
            Boolean rerunSuccessFlag = false;
            try {
                rerunSuccessFlag = (Boolean) futures.get(i).get();
            } catch (Exception e) {
                // do nothing
            }

            if (rerunSuccessFlag) {
                successCount += 1;
            }
        }

        // 3. Validate
        assertThat(successCount, is(1));
    }

    private TaskRun prepareTaskRunAndTaskAttempt(TaskRunStatus runStatus) {
        long taskId = WorkflowIdGenerator.nextTaskId();
        long taskRunId = WorkflowIdGenerator.nextTaskRunId();
        long taskAttemptId = WorkflowIdGenerator.nextTaskAttemptId(taskRunId, 1);

        Task task = MockTaskFactory.createTask().cloneBuilder().withId(taskId)
                .withName("test task")
                .withDescription("")
                .withOperatorId(1L)
                .withScheduleConf(new ScheduleConf(ScheduleType.NONE, null))
                .withConfig(Config.EMPTY)
                .withDependencies(new ArrayList<>())
                .withTags(new ArrayList<>())
                .withRetries(0)
                .withRetryDelay(1)
                .build();
        taskDao.create(task);

        TaskRun taskRun = TaskRun.newBuilder()
                .withId(taskRunId)
                .withTask(task)
                .withStatus(runStatus)
                .withConfig(Config.EMPTY)
                .withScheduleType(task.getScheduleConf().getType())
                .withDependentTaskRunIds(Collections.emptyList())
                .withInlets(Collections.emptyList())
                .withOutlets(Collections.emptyList())
                .withScheduledTick(new Tick(""))
                .withScheduleTime(new Tick(""))
                .build();
        taskRunDao.createTaskRun(taskRun);
        taskRunDao.createAttempt(TaskAttempt.newBuilder()
                .withId(taskAttemptId)
                .withTaskRun(taskRun)
                .withAttempt(1)
                .withStatus(runStatus)
                .withRetryTimes(0)
                .build()
        );
        return taskRun;
    }

    private void mockExecutorOnSubmit() {
        Mockito.doAnswer(invocation -> {
            TaskAttempt attemptArgument = invocation.getArgument(0);
            TaskAttemptProps persistedLatestAttempt = taskRunDao.fetchLatestTaskAttempt(attemptArgument.getTaskRun().getId());
            return persistedLatestAttempt.getId().equals(attemptArgument.getId());
        }).when(executor).submit(Mockito.isA(TaskAttempt.class));
    }

    @Test
    public void getLineCountOfFile_shouldBeAbleToReadEntireLogFile() throws IOException {
        InputStream mockLogResourceStream = getClass().getResourceAsStream("/testcase/example.log");

        BufferedReader reader = new BufferedReader(new InputStreamReader(mockLogResourceStream));
        Triple<List<String>, Integer, Integer> result = taskRunService.readLinesFromLogFile(reader, 30, 0, null);
        assertThat(result.getLeft().size(), is(30));
        assertThat(result.getLeft().get(0), is("This is line 1."));
        assertThat(result.getLeft().get(29), is("This is line 30."));
        assertThat(result.getMiddle(), is(0));
        assertThat(result.getRight(), is(29));
    }

    @Test
    public void getLineCountOfFile_withRange_shouldBeAbleToRead() throws IOException {
        InputStream mockLogResourceStream = getClass().getResourceAsStream("/testcase/example.log");

        BufferedReader reader = new BufferedReader(new InputStreamReader(mockLogResourceStream));
        Triple<List<String>, Integer, Integer> result = taskRunService.readLinesFromLogFile(reader, 30, 1, 4);
        // Should read line 2, line 3, line 4
        assertThat(result.getLeft().size(), is(3));
        assertThat(result.getLeft().get(0), is("This is line 2."));
        assertThat(result.getLeft().get(2), is("This is line 4."));
        assertThat(result.getMiddle(), is(1));
        assertThat(result.getRight(), is(3));
    }

    @Test
    public void getLineCountOfFile_withNegativeIndexes_shouldBeAbleToRead() throws IOException {
        InputStream mockLogResourceStream = getClass().getResourceAsStream("/testcase/example.log");

        BufferedReader reader = new BufferedReader(new InputStreamReader(mockLogResourceStream));
        Triple<List<String>, Integer, Integer> result = taskRunService.readLinesFromLogFile(reader, 30, 1, -1);
        assertThat(result.getLeft().size(), is(28));
        assertThat(result.getLeft().get(0), is("This is line 2."));
        assertThat(result.getLeft().get(27), is("This is line 29."));
        assertThat(result.getMiddle(), is(1));
        assertThat(result.getRight(), is(28));
    }

    @Test
    public void getLineCountOfFile_withNegativeIndexes_shouldBeAbleToReadLast5Lines() throws IOException {
        InputStream mockLogResourceStream = getClass().getResourceAsStream("/testcase/example.log");

        BufferedReader reader = new BufferedReader(new InputStreamReader(mockLogResourceStream));
        Triple<List<String>, Integer, Integer> result = taskRunService.readLinesFromLogFile(reader, 30, -5, null);
        assertThat(result.getLeft().size(), is(5));
        assertThat(result.getLeft().get(0), is("This is line 26."));
        assertThat(result.getLeft().get(4), is("This is line 30."));
        assertThat(result.getMiddle(), is(25));
        assertThat(result.getRight(), is(29));
    }

    @Test
    public void taskRunConvertToVo_success() {
        List<Task> tasks = MockTaskFactory.createTasksWithRelations(2, "0>>1");
        Task task1 = tasks.get(0);
        Task task2 = tasks.get(1);
        TaskRun taskRun1 = MockTaskRunFactory.createTaskRun(task1)
                .cloneBuilder()
                .withStatus(TaskRunStatus.FAILED)
                .build();
        TaskRun taskRun2 = MockTaskRunFactory.createTaskRun(task2)
                .cloneBuilder()
                .withStatus(TaskRunStatus.UPSTREAM_FAILED)
                .withFailedUpstreamTaskRunIds(Arrays.asList(taskRun1.getId()))
                .build();
        TaskAttempt taskAttempt1 = MockTaskAttemptFactory.createTaskAttempt(taskRun1);
        taskDao.create(task1);
        taskDao.create(task2);
        taskRunDao.createTaskRun(taskRun1);
        taskRunDao.createTaskRun(taskRun2);
        taskRunDao.createAttempt(taskAttempt1);

        TaskRunVO taskRunVO2 = taskRunService.convertToVO(taskRun2);

        assertThat(taskRunVO2.getTask().getId(), is(task2.getId()));
        assertThat(taskRunVO2.getFailedUpstreamTaskRuns().get(0).getId(), is(taskRun1.getId()));
    }

    @Test
    public void taskRunConvertToVoWithoutAttempt_success() {
        List<Task> tasks = MockTaskFactory.createTasksWithRelations(2, "0>>1");
        Task task1 = tasks.get(0);
        Task task2 = tasks.get(1);
        TaskRun taskRun1 = MockTaskRunFactory.createTaskRun(task1)
                .cloneBuilder()
                .withStatus(TaskRunStatus.FAILED)
                .build();
        TaskRun taskRun2 = MockTaskRunFactory.createTaskRun(task2)
                .cloneBuilder()
                .withStatus(TaskRunStatus.UPSTREAM_FAILED)
                .withFailedUpstreamTaskRunIds(Arrays.asList(taskRun1.getId()))
                .build();
        TaskAttempt taskAttempt1 = MockTaskAttemptFactory.createTaskAttempt(taskRun1);
        TaskAttempt taskAttempt2 = MockTaskAttemptFactory.createTaskAttempt(taskRun2);
        taskDao.create(task1);
        taskDao.create(task2);
        taskRunDao.createTaskRun(taskRun1);
        taskRunDao.createTaskRun(taskRun2);
        taskRunDao.createAttempt(taskAttempt1);
        taskRunDao.createAttempt(taskAttempt2);

        TaskRunVO taskRunVO2WithoutAttempt = taskRunService.convertToVOWithoutAttempt(taskRun2);

        MatcherAssert.assertThat(taskRunVO2WithoutAttempt.getTask().getId(), is(task2.getId()));
        MatcherAssert.assertThat(taskRunVO2WithoutAttempt.getAttempts().isEmpty(), is(true));
    }

    @Test
    public void taskRunsConvertToVOs_success() {
        List<Task> tasks = MockTaskFactory.createTasksWithRelations(2, "0>>1");
        Task task1 = tasks.get(0);
        Task task2 = tasks.get(1);
        TaskRun taskRun1 = MockTaskRunFactory.createTaskRun(task1)
                .cloneBuilder()
                .withStatus(TaskRunStatus.FAILED)
                .withFailedUpstreamTaskRunIds(null)
                .build();
        TaskRun taskRun2 = MockTaskRunFactory.createTaskRun(task2)
                .cloneBuilder()
                .withStatus(TaskRunStatus.UPSTREAM_FAILED)
                .withFailedUpstreamTaskRunIds(Arrays.asList(taskRun1.getId()))
                .build();
        TaskAttempt taskAttempt1 = MockTaskAttemptFactory.createTaskAttempt(taskRun1);
        taskDao.create(task1);
        taskDao.create(task2);
        taskRunDao.createTaskRun(taskRun1);
        taskRunDao.createTaskRun(taskRun2);
        taskRunDao.createAttempt(taskAttempt1);

        List<TaskRunVO> taskRunVOs = taskRunService.convertToVO(Arrays.asList(taskRun1, taskRun2));
        assertThat(taskRunVOs.size(), is(2));
        assertThat(taskRunVOs.get(0).getTask().getId(), is(task1.getId()));
        assertThat(taskRunVOs.get(1).getFailedUpstreamTaskRuns().get(0).getId(), is(taskRun1.getId()));
    }

    @Test
    public void getGlobalTaskRunGantt_shouldSuccess() {
        //prepare 9 taskruns success for 3 turns with different time point
        List<Task> tasks = MockTaskFactory.createTasksWithRelations(3, "0>>1;1>>2")
                .stream()
                .map(t -> t.cloneBuilder()
                        .withScheduleConf(new ScheduleConf(ScheduleType.SCHEDULED, "0 0 0 * * ?", "Asia/Kuching"))
                        .build())
                .collect(Collectors.toList());
        for (Task task : tasks) {
            taskDao.create(task);
        }
        OffsetDateTime baseTime = DateTimeUtils.now();
        //prepare task runs for three turns
        //every turn with 3 task runs have 1 day gap
        for (int day = 0; day < 3; day++) {
            List<TaskRun> taskRuns = MockTaskRunFactory.createTaskRunsWithRelations(tasks, "0>>1;1>>2");
            // create 3 task runs for very turn, with 1 hour process gap
            for (int task_index = 0; task_index < 3; task_index++) {
                int processBase_hour = 1;
                TaskRun taskRun = taskRuns.get(task_index).cloneBuilder()
                        .withCreatedAt(baseTime.minusDays(3-day).plusHours(processBase_hour++)).build();
                taskRunDao.createTaskRun(taskRun);
                // average time is 0 when no prev success taskrun
                if (day == 0) {
                    taskRunDao.updateTaskRunStat(taskRun.getId(), 0L, 0L);
                } else {
                    taskRunDao.updateTaskRunStat(taskRun.getId(), 3600L, 3600L);
                }
                taskRun = taskRun.cloneBuilder()
                        .withStatus(TaskRunStatus.SUCCESS)
                        .withQueuedAt(baseTime.minusDays(3-day).plusHours(processBase_hour++))
                        .withStartAt(baseTime.minusDays(3-day).plusHours(processBase_hour++))
                        .withEndAt(baseTime.minusDays(3-day).plusHours(processBase_hour++)).build();
                taskRunDao.updateTaskRun(taskRun);
            }
        }

        //fetch gantt info for 3 days, contain 9 taskruns
        TaskRunGanttChartVO result1 = taskRunService.getGlobalTaskRunGantt(baseTime.minusDays(3), baseTime, TimeType.createdAt);
        assertThat(result1.getInfoList().size(), is(9));
        assertThat(result1.getEarliestTime(), is(baseTime.minusDays(3).plusHours(1)));
        assertThat(result1.getLatestTime(), is(baseTime.minusDays(1).plusHours(4)));

        //fetch gantt info for 2 days, contain 6 taskruns
        TaskRunGanttChartVO result2 = taskRunService.getGlobalTaskRunGantt(baseTime.minusDays(2), baseTime, TimeType.queuedAt);
        assertThat(result2.getInfoList().size(), is(6));

        //fetch gantt info for 1 days, contain 3 taskruns
        TaskRunGanttChartVO result3 = taskRunService.getGlobalTaskRunGantt(baseTime.minusDays(1), baseTime, TimeType.startAt);
        assertThat(result3.getInfoList().size(), is(3));
    }

    @Test
    public void getSpecifiedTaskRunGanttChart_withAllSuccess_shouldSuccess() {

        List<Task> tasks = MockTaskFactory.createTasksWithRelations(5, "0>>1;1>>2;2>>3;3>>4")
                .stream()
                .map(t -> t.cloneBuilder()
                        .withScheduleConf(new ScheduleConf(ScheduleType.SCHEDULED, "0 0 0 * * ?", "Asia/Kuching"))
                        .build())
                .collect(Collectors.toList());
        List<TaskRun> taskRuns = MockTaskRunFactory.createTaskRunsWithRelations(tasks, "0>>1;1>>2;2>>3;3>>4");
        OffsetDateTime baseTime = DateTimeUtils.now();
        DateTimeUtils.freeze();
        for (Task task : tasks) {
            taskDao.create(task);
        }
        // 5 taskruns 0>>1>>2>>3>>4
        // task run 0 created 75h ago, queued 74h ago, start 72h ago, end 69h ago
        // task run 1 created 60h ago, queued 59h ago, start 57h ago, end 54h ago
        // task run 2 created 45h ago, queued 44h ago, start 42h ago, end 39h ago
        // task run 3 created 30h ago, queued 29h ago, start 27h ago, end 24h ago
        // task run 4 created 15h ago, queued 14h ago, start 12h ago, end 9h ago
        for (int taskRun_index = 0; taskRun_index < taskRuns.size(); taskRun_index++) {
            Long taskRunGap_hours = 15L;
            TaskRun taskRun = taskRuns.get(taskRun_index);
            taskRunDao.createTaskRun(taskRun);
            taskRun = taskRun.cloneBuilder()
                    .withStatus(TaskRunStatus.SUCCESS)
                    .withCreatedAt(baseTime.minusHours(taskRunGap_hours*(5-taskRun_index)))
                    .withQueuedAt(baseTime.minusHours(taskRunGap_hours*(5-taskRun_index)-1))
                    .withStartAt(baseTime.minusHours(taskRunGap_hours*(5-taskRun_index)-3))
                    .withEndAt(baseTime.minusHours(taskRunGap_hours*(5-taskRun_index)-6))
                    .build();
            taskRunDao.updateTaskRun(taskRun);
            if (taskRun_index == 0) {
                taskRunDao.updateTaskRunStat(taskRun.getId(), 0L, 0L);
            } else {
                taskRunDao.updateTaskRunStat(taskRun.getId(), 3600L, 3600L);
            }
        }

        //fetch the up&downstream for taskRun2, taskRun 0 is created 30h earlier > 24h, excluded
        TaskRunGanttChartVO result1 = taskRunService.getTaskRunGantt(taskRuns.get(2).getId(), 24);
        assertThat(result1.getInfoList().size(), is(4));

        //fetch the up&downstream for taskRun0, all downstream included
        TaskRunGanttChartVO result2 = taskRunService.getTaskRunGantt(taskRuns.get(0).getId(), 24);
        assertThat(result2.getInfoList().size(), is(5));

        //fetch the up&downstream for taskRun4, taskRun0,1,2 are created 24h before, excluded
        TaskRunGanttChartVO result3 = taskRunService.getTaskRunGantt(taskRuns.get(4).getId(), 24);
        assertThat(result3.getInfoList().size(), is(2));
    }

    @Test
    public void getSpecifiedTaskRunGanttChart_withFailedCase_shouldSuccess() {

        List<Task> tasks = MockTaskFactory.createTasksWithRelations(6, "0>>1;1>>2;2>>3;3>>4;4>>5")
                .stream()
                .map(t -> t.cloneBuilder()
                        .withScheduleConf(new ScheduleConf(ScheduleType.SCHEDULED, "0 0 0 * * ?", "Asia/Kuching"))
                        .build())
                .collect(Collectors.toList());
        List<TaskRun> taskRuns = MockTaskRunFactory.createTaskRunsWithRelations(tasks, "0>>1;1>>2;2>>3;3>>4;4>>5");
        OffsetDateTime baseTime = DateTimeUtils.now();
        DateTimeUtils.freeze();
        for (Task task : tasks) {
            taskDao.create(task);
        }
        // 5 taskruns 0>>1>>2>>3>>4
        // task run 0 created 75h ago, queued 74h ago, start 72h ago, end 69h ago
        // task run 1 created 60h ago, queued 59h ago, start 57h ago, end 54h ago
        // task run 2 created 45h ago, queued 44h ago, start 42h ago, end 39h ago Failed
        // task run 3 created 30h ago, end 24h ago with upstream_failed
        // task run 4 created 15h ago, end 15h ago with upstream_failed
        // task run 5 created now, end now with upstream_failed
        for (int taskRun_index = 0; taskRun_index < taskRuns.size(); taskRun_index++) {
            Long taskRunGap_hours = 15L;
            TaskRun taskRun = taskRuns.get(taskRun_index);
            taskRunDao.createTaskRun(taskRun);
            if (taskRun_index < 2) {
                taskRun = taskRun.cloneBuilder()
                        .withStatus(TaskRunStatus.SUCCESS)
                        .withCreatedAt(baseTime.minusHours(taskRunGap_hours*(5-taskRun_index)))
                        .withQueuedAt(baseTime.minusHours(taskRunGap_hours*(5-taskRun_index)-1))
                        .withStartAt(baseTime.minusHours(taskRunGap_hours*(5-taskRun_index)-3))
                        .withEndAt(baseTime.minusHours(taskRunGap_hours*(5-taskRun_index)-6))
                        .build();
            } else if (taskRun_index == 2) {
                taskRun = taskRun.cloneBuilder()
                        .withStatus(TaskRunStatus.FAILED)
                        .withCreatedAt(baseTime.minusHours(taskRunGap_hours*(5-taskRun_index)))
                        .withQueuedAt(baseTime.minusHours(taskRunGap_hours*(5-taskRun_index)-1))
                        .withStartAt(baseTime.minusHours(taskRunGap_hours*(5-taskRun_index)-3))
                        .withEndAt(baseTime.minusHours(taskRunGap_hours*(5-taskRun_index)-6))
                        .build();
            } else {
                taskRun = taskRun.cloneBuilder()
                        .withStatus(TaskRunStatus.UPSTREAM_FAILED)
                        .withCreatedAt(baseTime.minusHours(taskRunGap_hours*(5-taskRun_index)))
                        .withEndAt(baseTime.minusHours(taskRunGap_hours*(5-taskRun_index)))
                        .build();
            }
            taskRunDao.updateTaskRun(taskRun);
            if (taskRun_index == 0) {
                taskRunDao.updateTaskRunStat(taskRun.getId(), 0L, 0L);
            } else {
                taskRunDao.updateTaskRunStat(taskRun.getId(), 3600L, 3600L);
            }
        }

        //fetch the up&downstream for taskRun2, taskRun 0 is created 30h earlier > 24h, excluded
        TaskRunGanttChartVO result1 = taskRunService.getTaskRunGantt(taskRuns.get(2).getId(), 24);
        assertThat(result1.getInfoList().size(), is(5));
        assertThat(result1.getInfoList().get(2).getTaskRunId(), is(taskRuns.get(3).getId()));

        //fetch the up&downstream for taskRun0, all downstream included
        TaskRunGanttChartVO result2 = taskRunService.getTaskRunGantt(taskRuns.get(0).getId(), 24);
        assertThat(result2.getInfoList().size(), is(6));

        //fetch the up&downstream for taskRun4, taskRun0,1,2 are created 24h before, excluded
        TaskRunGanttChartVO result3 = taskRunService.getTaskRunGantt(taskRuns.get(4).getId(), 24);
        assertThat(result3.getInfoList().size(), is(3));

        //fetch the up&downstream for taskRun4, trace 72h, all included
        TaskRunGanttChartVO result4 = taskRunService.getTaskRunGantt(taskRuns.get(5).getId(), 72);
        assertThat(result4.getInfoList().size(), is(5));
    }

    @Test
    public void getTaskRunWithAllDownstream_shouldInTopologicalOrder() {
        //prepare
        List<Task> tasks = MockTaskFactory.createTasksWithRelations(5, "0>>1;0>>2;1>>3;0>>4;3>>4;2>>4");
        List<TaskRun> taskRuns = MockTaskRunFactory.createTaskRunsWithRelations(tasks, "0>>1;0>>2;1>>3;0>>4;3>>4;2>>4");
        for (TaskRun taskRun : taskRuns) {
            taskDao.create(taskRun.getTask());
            taskRunDao.createTaskRun(taskRun);
        }

        // input is in topological order 0 1 2 3 4
        // taskRunDao.fetchDownStreamTaskRunIdsRecursive(taskRuns.get(0).getId());
        // fetch result order is 0 1 2 4 3 not in topological order

        //process
        List<TaskRun> results = taskRunService.getTaskRunWithAllDownstream(taskRuns.get(0).getId(), Collections.singleton(TaskRunStatus.CREATED));

        //verify: result is in topological order
        assertThat(results.get(0).getId(), is(taskRuns.get(0).getId()));
        assertThat(results.get(3).getId(), is(taskRuns.get(3).getId()));
        assertThat(results.get(4).getId(), is(taskRuns.get(4).getId()));
    }

    @Disabled
    @Test
    public void rerunWithDownstream_shouldSuccess() {
        List<Task> taskList = MockTaskFactory.createTasksWithRelations(2, "0>>1");
        taskList.forEach(task -> taskDao.create(task));
        List<TaskRun> taskRunList = MockTaskRunFactory.createTaskRunsWithRelations(taskList, "0>>1");
        TaskRun taskRun1 = taskRunList.get(0).cloneBuilder().withStatus(TaskRunStatus.FAILED).build();
        TaskRun taskRun2 = taskRunList.get(1).cloneBuilder()
                .withStatus(TaskRunStatus.UPSTREAM_FAILED)
                .withFailedUpstreamTaskRunIds(Collections.singletonList(taskRun1.getId())).build();
        taskRunDao.createTaskRun(taskRun1);
        taskRunDao.createTaskRun(taskRun2);
        TaskAttempt taskAttempt1 = MockTaskAttemptFactory.createTaskAttempt(taskRun1);
        TaskAttempt taskAttempt2 = MockTaskAttemptFactory.createTaskAttempt(taskRun2);
        taskRunDao.createAttempt(taskAttempt1);
        taskRunDao.createAttempt(taskAttempt2);

        doAnswer(invocation -> {
            TaskAttempt taskAttempt = invocation.getArgument(0, TaskAttempt.class);
            taskRunDao.updateTaskAttemptStatus(taskAttempt.getId(), TaskRunStatus.SUCCESS);
            eventBus.post(new TaskAttemptStatusChangeEvent(taskAttempt.getId(), TaskRunStatus.RUNNING, TaskRunStatus.SUCCESS, taskAttempt.getTaskName(), taskAttempt.getTaskId()));
            return null;
        }).when(executor).submit(ArgumentMatchers.any());

        //TODO: 执行TaskManager retry方法时 post的taskRunTransitionEvent没有被TaskRunStateMachine(注入且start了)接收到
        taskRunService.rerunTaskRuns(Arrays.asList(taskRun1.getId(), taskRun2.getId()));
        System.out.println(taskRunDao.fetchTaskRunById(taskRun1.getId()).get().getStatus());


        await().atMost(120, TimeUnit.SECONDS).until(() -> {
            Optional<TaskRunStatus> s = taskRunDao.fetchTaskAttemptStatus(taskRun2.getId() + 2);
            return s.isPresent() && (s.get().isFinished());
        });

        TaskAttemptProps attemptProps2 = taskRunDao.fetchLatestTaskAttempt(taskRun2.getId());
        assertThat(attemptProps2.getId(), Matchers.is(taskRun2.getId()+2));
        assertThat(attemptProps2.getAttempt(), Matchers.is(2));
        assertThat(attemptProps2.getStatus(), Matchers.is(TaskRunStatus.SUCCESS));
    }

    @Test
    public void skipNotExistedTaskRun_shouldFalse() {
        TaskRun taskRun = prepareData().cloneBuilder()
                .withStatus(TaskRunStatus.FAILED).build();
        Boolean result = taskRunService.skipTaskRun(2L);
        assertThat(result, is(false));
    }

    @Test
    public void skipTaskRun_shouldSuccess() {
        Task task = MockTaskFactory.createTask();
        TaskRun taskRun = MockTaskRunFactory.createTaskRun(task).cloneBuilder()
                .withStatus(TaskRunStatus.FAILED).build();
        taskDao.create(task);
        taskRunDao.createTaskRun(taskRun);
        taskRunDao.createAttempt(MockTaskAttemptFactory.createTaskAttempt(taskRun));
        Boolean result = taskRunService.skipTaskRun(taskRun.getId());
        assertThat(result, is(true));
    }


}
