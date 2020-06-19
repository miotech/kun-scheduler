package com.miotech.kun.workflow.common.taskrun.service;

import com.google.inject.Inject;
import com.miotech.kun.workflow.common.CommonTestBase;
import com.miotech.kun.workflow.common.resource.ResourceLoader;
import com.miotech.kun.workflow.common.task.dao.TaskDao;
import com.miotech.kun.workflow.common.taskrun.bo.TaskAttemptProps;
import com.miotech.kun.workflow.common.taskrun.dao.TaskRunDao;
import com.miotech.kun.workflow.common.taskrun.vo.TaskRunLogVO;
import com.miotech.kun.workflow.common.taskrun.vo.TaskRunVO;
import com.miotech.kun.workflow.core.model.common.Tick;
import com.miotech.kun.workflow.core.model.task.ScheduleConf;
import com.miotech.kun.workflow.core.model.task.ScheduleType;
import com.miotech.kun.workflow.core.model.task.Task;
import com.miotech.kun.workflow.core.model.taskrun.TaskRun;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunStatus;
import com.miotech.kun.workflow.core.resource.Resource;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class TaskRunServiceTest extends CommonTestBase {

    @Inject
    private TaskRunService taskRunService;

    @Inject
    private TaskDao taskDao;

    @Inject
    private ResourceLoader resourceLoader;

    private TaskRunDao taskRunDao = mock(TaskRunDao.class);

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Test
    public void testGetTaskRunDetail() {
        TaskRun taskRun = prepareData();
        TaskRunVO existedRun = taskRunService.getTaskRunDetail(taskRun.getId()).get();
        assertNotNull(existedRun);
        assertNotNull(existedRun.getTask());

        taskRunDao.deleteTaskRun(taskRun.getId());
    }

    private TaskRun prepareData() {
        long testId = 1L;

        Task task = Task.newBuilder().withId(22L)
                .withName("test task")
                .withDescription("")
                .withOperatorId(1L)
                .withScheduleConf(new ScheduleConf(ScheduleType.NONE, null))
                .withArguments(Collections.emptyList())
                .withVariableDefs(Collections.emptyList())
                .withDependencies(new ArrayList<>())
                .build();
        taskDao.create(task);

        TaskRun taskRun = TaskRun.newBuilder()
                .withId(testId)
                .withTask(task)
                .withStatus(TaskRunStatus.CREATED)
                .withVariables(Collections.emptyList())
                .withDependentTaskRunIds(Collections.emptyList())
                .withInlets(Collections.emptyList())
                .withOutlets(Collections.emptyList())
                .withScheduledTick(new Tick(""))
                .build();
        Mockito.when(taskRunDao.fetchById(taskRun.getId()))
                .thenReturn(Optional.of(taskRun));
        return taskRun;
    }

    @Test
    public void getTaskRunLog() throws IOException {
        Long taskRunId = 1L;
        String testStr = "hellow world";
        Resource resource = creatResource("xyz", testStr, 1);

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
        taskRunLogVO = taskRunService.getTaskRunLog(taskRunId,-1,0, 3);
        assertEquals(1, taskRunLogVO.getLogs().size());
        assertEquals(2, taskRunLogVO.getAttempt());
        assertEquals(testStr, taskRunLogVO.getLogs().get(0));

    }


    @Test
    public void getTaskRunLog_withstartLine() throws IOException {
        Long taskRunId = 1L;
        Resource resource = creatResource("xyz", "hello world\n", 3);

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

    @Test
    public void getTaskRunLog_withendLine() throws IOException {
        Long taskRunId = 1L;
        Resource resource = creatResource("xyz", "hello world\n", 3);


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
        assertEquals(2, taskRunLogVO.getLogs().size());

        taskRunLogVO = taskRunService.getTaskRunLog(taskRunId, 1, 0, 4);
        assertEquals(3, taskRunLogVO.getLogs().size());
    }

    @Test
    public void getTaskRunLog_withIllegalArg() throws IOException {
        Long taskRunId = 1L;

        try {
            taskRunService.getTaskRunLog(taskRunId,-1, -1, 0);
        } catch (Exception e) {
            assertEquals(IllegalArgumentException.class, e.getClass());
            assertEquals("startLine should larger or equal to 0", e.getMessage());
        }

        try {
            taskRunService.getTaskRunLog(taskRunId,-1, 1, 0);
        } catch (Exception e) {
            assertEquals(IllegalArgumentException.class, e.getClass());
            assertEquals("endLine should not smaller than startLine", e.getMessage());
        }

        Mockito.when(taskRunDao.fetchAttemptsPropByTaskRunId(taskRunId))
                .thenReturn(Collections.emptyList());
        try {
            taskRunService.getTaskRunLog(taskRunId,-1, 1, 2);
        } catch (Exception e) {
            assertEquals(IllegalArgumentException.class, e.getClass());
            assertEquals("No valid task attempt found for TaskRun \"1\"", e.getMessage());
        }
    }

    private Resource creatResource(String fileName, String text, int times) throws IOException {
        File file = tempFolder.newFile(fileName);
        Resource resource = resourceLoader.getResource("file://" + file.getPath(), true);
        Writer writer= new PrintWriter(resource.getOutputStream());

        for (int i=0; i < times; i++) {
            writer.write(text);
        }
        writer.flush();
        return resource;
    }
}