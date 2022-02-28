package com.miotech.kun.dataplatform.web.common.taskdefinition.service;

import com.miotech.kun.dataplatform.AppTestBase;
import com.miotech.kun.dataplatform.facade.model.taskdefinition.*;
import com.miotech.kun.dataplatform.mocking.MockTaskDefinitionFactory;
import com.miotech.kun.dataplatform.mocking.MockTaskRunFactory;
import com.miotech.kun.dataplatform.web.common.commit.service.TaskCommitService;
import com.miotech.kun.dataplatform.web.common.commit.vo.CommitRequest;
import com.miotech.kun.dataplatform.web.common.deploy.service.DeployService;
import com.miotech.kun.dataplatform.web.common.taskdefinition.dao.TaskDefinitionDao;
import com.miotech.kun.dataplatform.web.common.taskdefinition.dao.TaskRelationDao;
import com.miotech.kun.dataplatform.web.common.taskdefinition.vo.*;
import com.miotech.kun.dataplatform.web.common.utils.DataPlatformIdGenerator;
import com.miotech.kun.monitor.facade.sla.SlaFacade;
import com.miotech.kun.security.testing.WithMockTestUser;
import com.miotech.kun.workflow.client.WorkflowClient;
import com.miotech.kun.workflow.client.model.RunTaskRequest;
import com.miotech.kun.workflow.client.model.Task;
import com.miotech.kun.workflow.client.model.TaskRun;
import com.miotech.kun.workflow.client.model.TaskRunLogRequest;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunStatus;
import com.miotech.kun.workflow.utils.WorkflowIdGenerator;
import org.json.simple.JSONObject;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.miotech.kun.dataplatform.web.common.tasktemplate.dao.TaskTemplateDaoTest.TEST_TEMPLATE;
import static com.miotech.kun.workflow.core.model.taskrun.TaskRunStatus.*;
import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.in;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

// TODO: figure out a solution to bootstrap Workflow facade related tests
@WithMockTestUser
public class TaskDefinitionServiceTest extends AppTestBase {

    @Autowired
    private TaskDefinitionService taskDefinitionService;

    @Autowired
    private TaskDefinitionDao taskDefinitionDao;

    @Autowired
    private TaskRelationDao taskRelationDao;

    @Autowired
    private WorkflowClient workflowClient;

    @Autowired
    private DeployService deployService;

    @Autowired
    private TaskCommitService taskCommitService;

    @Autowired
    private SlaFacade slaFacade;

    @Test
    public void create_ok() {
        CreateTaskDefinitionRequest taskDefinitionProps = new CreateTaskDefinitionRequest("test", TEST_TEMPLATE);
        TaskDefinition taskDefinition = taskDefinitionService.create(taskDefinitionProps);
        assertTrue(taskDefinition.getId() > 0);
        assertTrue(taskDefinition.getDefinitionId() > 0);
        assertThat(taskDefinition.getTaskTemplateName(), is("SparkSQL"));
        assertThat(taskDefinition.getName(), is("test"));
    }

    @Test
    public void create_checkWithSingleDuplicateTaskDefinition() {
        CreateTaskDefinitionRequest taskDefinitionProps = new CreateTaskDefinitionRequest("test", TEST_TEMPLATE);
        taskDefinitionService.create(taskDefinitionProps);
        CreateTaskDefinitionRequest taskDefinitionPropsDuplicate = new CreateTaskDefinitionRequest("test", TEST_TEMPLATE);
        assertThrows(IllegalArgumentException.class,() -> taskDefinitionService.create(taskDefinitionPropsDuplicate));

    }

    @Test
    public void create_checkDuplicateWithMultipleArchivedTask() {
        Random rand = new Random();
        for (int i = 0; i < rand.nextInt(5) + 5; i++) {
            CreateTaskDefinitionRequest taskDefinitionProps = new CreateTaskDefinitionRequest("test", TEST_TEMPLATE);
            TaskDefinition taskDefinition = taskDefinitionService.create(taskDefinitionProps);
            taskDefinitionService.delete(taskDefinition.getDefinitionId());
        }
        create_checkWithSingleDuplicateTaskDefinition();
    }

    @Test
    public void update_ok() {
        // prepare
        TaskDefinition taskDefinition = MockTaskDefinitionFactory.createTaskDefinition();
        taskDefinitionDao.create(taskDefinition);
        TaskPayload taskPayload = taskDefinition.getTaskPayload();
        Map<String, Object> taskConfig = taskPayload.getTaskConfig();
        taskConfig.put("sql", "select 2");
        TaskPayload updatedTaskPayload = taskPayload
                .cloneBuilder()
                .withTaskConfig(taskConfig)
                .build();
        UpdateTaskDefinitionRequest updateRequest = new UpdateTaskDefinitionRequest(
                taskDefinition.getDefinitionId(),
                taskDefinition.getName() + "_updated",
                updatedTaskPayload,
                1L
        );

        // invocation
        TaskDefinition updated = taskDefinitionService.update(updateRequest.getDefinitionId(),
                updateRequest);
        // verify
        assertTrue(taskDefinition.getId() > 0);
        assertThat(updated.getTaskTemplateName(), is(taskDefinition.getTaskTemplateName()));
        assertThat(updated.getName(), is(updateRequest.getName()));
        assertThat(updated.getOwner(), is(updateRequest.getOwner()));
        assertThat(updated.getTaskPayload(), sameBeanAs(updateRequest.getTaskPayload()));
    }

    @Test
    public void update_checkWithSingleDuplicateTaskDefinition() {
        TaskDefinition taskDefinition = MockTaskDefinitionFactory.createTaskDefinition();
        taskDefinitionDao.create(taskDefinition);
        TaskDefinition taskDefinitionUnchanged = MockTaskDefinitionFactory.createTaskDefinition();
        taskDefinitionDao.create(taskDefinitionUnchanged);
        TaskPayload taskPayload = taskDefinition.getTaskPayload();
        Map<String, Object> taskConfig = taskPayload.getTaskConfig();
        taskConfig.put("sql", "select 2");
        TaskPayload updatedTaskPayload = taskPayload
                .cloneBuilder()
                .withTaskConfig(taskConfig)
                .build();
        UpdateTaskDefinitionRequest updateRequest = new UpdateTaskDefinitionRequest(
                taskDefinition.getDefinitionId(),
                taskDefinitionUnchanged.getName(), //used name
                updatedTaskPayload,
                1L
        );
        assertThrows(IllegalArgumentException.class, () -> taskDefinitionService.update(updateRequest.getDefinitionId(), updateRequest));
    }

    @Test
    public void update_checkDuplicateWithMultipleArchivedTask() {
        //generate multiple task definition, update them to same name and delete
        Random rand = new Random();
        int times = rand.nextInt(5) + 5;
        //prepare multiple archived tasks
        for (int time = 1; time < times; time++) {
            TaskDefinition taskDefinitionTemp = MockTaskDefinitionFactory.createTaskDefinition();
            taskDefinitionDao.create(taskDefinitionTemp);
            TaskPayload taskPayload = taskDefinitionTemp.getTaskPayload();
            Map<String, Object> taskConfig = taskPayload.getTaskConfig();
            taskConfig.put("sql", "select 2");
            TaskPayload updatedTaskPayload = taskPayload
                    .cloneBuilder()
                    .withTaskConfig(taskConfig)
                    .build();
            UpdateTaskDefinitionRequest updateRequest = new UpdateTaskDefinitionRequest(
                    taskDefinitionTemp.getDefinitionId(),
                    "test", //same name use multiple times
                    updatedTaskPayload,
                    1L
            );
            taskDefinitionService.update(updateRequest.getDefinitionId(), updateRequest);
            // for the last, do not delete, check whether update successfully on last time
            if (time == times - 1) continue;
            taskDefinitionService.delete(taskDefinitionTemp.getDefinitionId());
        }

        //update taskDefinition with duplicate name
        TaskDefinition taskDefinitionTemp = MockTaskDefinitionFactory.createTaskDefinition();
        taskDefinitionDao.create(taskDefinitionTemp);
        TaskPayload taskPayload = taskDefinitionTemp.getTaskPayload();
        Map<String, Object> taskConfig = taskPayload.getTaskConfig();
        taskConfig.put("sql", "select 2");
        TaskPayload updatedTaskPayload = taskPayload
                .cloneBuilder()
                .withTaskConfig(taskConfig)
                .build();
        UpdateTaskDefinitionRequest updateRequest = new UpdateTaskDefinitionRequest(
                taskDefinitionTemp.getDefinitionId(),
                "test", //same name use multiple times
                updatedTaskPayload,
                1L
        );

        //verify
        assertThrows(IllegalArgumentException.class,() -> taskDefinitionService.update(updateRequest.getDefinitionId(), updateRequest));

    }

    @Test
    public void test_update_withInputNodes() {
        // prepare with dependencies
        TaskDefinition upstreamTaskDefinition = MockTaskDefinitionFactory.createTaskDefinition();
        taskDefinitionDao.create(upstreamTaskDefinition);

        TaskDefinition taskDefinition = MockTaskDefinitionFactory.createTaskDefinition();
        taskDefinitionDao.create(taskDefinition);
        TaskPayload taskPayload = taskDefinition.getTaskPayload();
        Map<String, Object> taskConfig = taskPayload.getTaskConfig();
        taskConfig.put("sql", "select 2");
        ScheduleConfig scheduleConfig = taskDefinition.getTaskPayload().getScheduleConfig().cloneBuilder()
                .withInputNodes(Collections.singletonList(upstreamTaskDefinition.getDefinitionId()))
                .build();
        TaskPayload updatedTaskPayload = taskPayload
                .cloneBuilder()
                .withTaskConfig(taskConfig)
                .withScheduleConfig(scheduleConfig)
                .build();

        UpdateTaskDefinitionRequest updateRequest = new UpdateTaskDefinitionRequest(
                taskDefinition.getDefinitionId(),
                taskDefinition.getName() + "_updated",
                updatedTaskPayload,
                1L
        );

        // invocation
        TaskDefinition updated = taskDefinitionService.update(updateRequest.getDefinitionId(),
                updateRequest);
        // verify
        assertTrue(taskDefinition.getId() > 0);
        assertThat(updated.getTaskTemplateName(), is(taskDefinition.getTaskTemplateName()));
        assertThat(updated.getName(), is(updateRequest.getName()));
        assertThat(updated.getOwner(), is(updateRequest.getOwner()));
        assertThat(updated.getTaskPayload(), sameBeanAs(updateRequest.getTaskPayload()));

        TaskRelation taskRelation = taskRelationDao.fetchByDownstreamId(taskDefinition.getDefinitionId()).get(0);
        assertEquals(upstreamTaskDefinition.getDefinitionId(), taskRelation.getUpstreamId());
        assertEquals(taskDefinition.getDefinitionId(), taskRelation.getDownstreamId());
    }

    @Test
    public void update_removeInputNode_relationShouldBeDeleted() {
        //prepare task def with input nodes
        TaskDefinition upstreamTaskDefinition = MockTaskDefinitionFactory.createTaskDefinition();
        taskDefinitionDao.create(upstreamTaskDefinition);
        TaskDefinition taskDefinition = MockTaskDefinitionFactory.createTaskDefinitions(1,
                Collections.singletonList(upstreamTaskDefinition.getDefinitionId())).get(0);
        taskDefinitionDao.create(taskDefinition);
        UpdateTaskDefinitionRequest updateRequest0 = new UpdateTaskDefinitionRequest(
                taskDefinition.getDefinitionId(),
                taskDefinition.getName(),
                taskDefinition.getTaskPayload(),
                taskDefinition.getOwner()
        );
        taskDefinitionService.update(taskDefinition.getDefinitionId(), updateRequest0);
        //process
        ScheduleConfig scheduleConfig = taskDefinition.getTaskPayload().getScheduleConfig().cloneBuilder()
                .withInputNodes(Collections.emptyList())
                .build();
        TaskPayload updatedTaskPayload = taskDefinition.getTaskPayload()
                .cloneBuilder()
                .withScheduleConfig(scheduleConfig)
                .build();
        UpdateTaskDefinitionRequest updateRequest = new UpdateTaskDefinitionRequest(
                taskDefinition.getDefinitionId(),
                taskDefinition.getName() + "_updated",
                updatedTaskPayload,
                1L
        );
        TaskDefinition updated = taskDefinitionService.update(updateRequest.getDefinitionId(),
                updateRequest);
        List<TaskRelation> taskRelations = taskRelationDao.fetchByDownstreamId(taskDefinition.getDefinitionId());
        //valid
        assertThat(updated.getDefinitionId(), is(taskDefinition.getDefinitionId()));
        assertThat(updated.getTaskTemplateName(), is(taskDefinition.getTaskTemplateName()));
        assertThat(updated.getName(), is(updateRequest.getName()));
        assertThat(updated.getOwner(), is(updateRequest.getOwner()));
        assertThat(updated.getTaskPayload(), sameBeanAs(updateRequest.getTaskPayload()));
        assertThat(taskRelations.size(), is(0));
    }

    @Disabled
    @Test
    public void testRun_ok() {
        TaskDefinition taskDefinition = MockTaskDefinitionFactory.createTaskDefinition();
        taskDefinitionDao.create(taskDefinition);
        TaskRunRequest runRequest = new TaskRunRequest();
        JSONObject params = new JSONObject();
        params.put("sparkSQL", "SELECT 1");
        runRequest.setParameters(params);

        TaskTry taskTry = taskDefinitionService.run(taskDefinition.getDefinitionId(), runRequest);

        assertTrue(taskTry.getId() > 0);
        assertTrue(taskTry.getDefinitionId() > 0);
        assertTrue(taskTry.getWorkflowTaskId() > 0);
        assertTrue(taskTry.getWorkflowTaskRunId() > 0);
    }

    @Test
    public void testRun_missing_params() {
        TaskDefinition taskDefinition = MockTaskDefinitionFactory.createTaskDefinition();
        taskDefinitionDao.create(taskDefinition);
        TaskRunRequest runRequest = new TaskRunRequest();

        try {
            taskDefinitionService.run(taskDefinition.getDefinitionId(), runRequest);
        } catch (Exception e) {
            assertThat(e.getClass(), is(IllegalArgumentException.class));
        }
    }

    @Disabled
    @Test
    public void testStop_ok() {
        TaskDefinition taskDefinition = MockTaskDefinitionFactory.createTaskDefinition();
        taskDefinitionDao.create(taskDefinition);
        TaskRunRequest runRequest = new TaskRunRequest();
        JSONObject params = new JSONObject();
        params.put("sparkSQL", "SELECT 1");
        runRequest.setParameters(params);

        TaskTry taskTry = taskDefinitionService.run(taskDefinition.getDefinitionId(), runRequest);
        taskDefinitionService.stop(taskTry.getId());

        await().atMost(10, TimeUnit.SECONDS)
                .until(() -> workflowClient
                        .getTaskRunState(taskTry.getWorkflowTaskRunId())
                        .getStatus().isFinished());
        TaskRun taskRun = workflowClient.getTaskRun(taskTry.getWorkflowTaskRunId());
        assertThat(taskRun.getStatus(), is(ABORTED));
    }

    @Disabled
    @Test
    public void test_RunLog_ok() {
        TaskDefinition taskDefinition = MockTaskDefinitionFactory.createTaskDefinition();
        taskDefinitionDao.create(taskDefinition);
        TaskRunRequest runRequest = new TaskRunRequest();
        JSONObject params = new JSONObject();
        params.put("sparkSQL", "SELECT 1");
        runRequest.setParameters(params);

        TaskTry taskTry = taskDefinitionService.run(taskDefinition.getDefinitionId(), runRequest);
        TaskRunLogRequest request = TaskRunLogRequest.newBuilder()
                .withTaskRunId(taskTry.getWorkflowTaskRunId())
                .withAttempt(-1)
                .build();
        await().atMost(10, TimeUnit.SECONDS)
                .until(() -> workflowClient
                        .getTaskRunState(taskTry.getWorkflowTaskRunId())
                        .getStatus().isFinished());
        TaskRunLogVO vo = taskDefinitionService.runLog(request);
        assertThat(vo.getStatus(), in(new TaskRunStatus[]{RUNNING, SUCCESS, FAILED}));
        assertThat(vo.getTaskRunId(), is(taskTry.getWorkflowTaskRunId()));
        assertTrue(vo.getLogs().size() > 0);
    }

    @Test
    public void test_convertToVO_notDeployed() {
        // create definition with dependencies
        TaskDefinition upstreamTaskDefinition = MockTaskDefinitionFactory.createTaskDefinition();
        taskDefinitionDao.create(upstreamTaskDefinition);
        TaskDefinition taskDefinition = MockTaskDefinitionFactory.createTaskDefinitions(1, Collections.singletonList(upstreamTaskDefinition.getDefinitionId()))
                .get(0);
        taskDefinitionDao.create(taskDefinition);

        TaskDefinitionVO vo = taskDefinitionService.convertToVO(taskDefinition);

        assertTrue(vo.getId() > 0);
        assertThat(vo.getName(), is(taskDefinition.getName()));
        assertThat(vo.getCreateTime(), is(taskDefinition.getCreateTime()));
        assertThat(vo.getLastModifier(), is(taskDefinition.getLastModifier()));
        assertThat(vo.getLastUpdateTime(), is(taskDefinition.getUpdateTime()));
        assertThat(vo.getOwner(), is(taskDefinition.getOwner()));
        assertThat(vo.getTaskTemplateName(), is(taskDefinition.getTaskTemplateName()));
        assertThat(vo.getTaskPayload(), sameBeanAs(taskDefinition.getTaskPayload()));
        assertThat(vo.getCreator(), is(taskDefinition.getCreator()));
        assertThat(vo.isArchived(), is(taskDefinition.isArchived()));
        assertThat(vo.isDeployed(), is(false));
        assertThat(vo.isUpdated(), is(true));
        assertThat(vo.getTaskCommits(), is(empty()));
        // upstreams
        assertThat(vo.getUpstreamTaskDefinitions().size(), is(1));
        assertThat(vo.getUpstreamTaskDefinitions().get(0).getId(), is(upstreamTaskDefinition.getDefinitionId()));
        assertThat(vo.getUpstreamTaskDefinitions().get(0).getName(), is(upstreamTaskDefinition.getName()));
    }

    @Test
    public void test_convertToVO_updatedButNotDeployed() {
        // create definition with dependencies
        TaskDefinition upstreamTaskDefinition = MockTaskDefinitionFactory.createTaskDefinition();
        taskDefinitionDao.create(upstreamTaskDefinition);
        TaskDefinition taskDefinition = MockTaskDefinitionFactory.createTaskDefinitions(1, Collections.singletonList(upstreamTaskDefinition.getDefinitionId()))
                .get(0);
        taskDefinitionDao.create(taskDefinition);

        // commit
        String msg = "commit test msg";
        taskCommitService.commit(taskDefinition.getDefinitionId(), msg);

        TaskDefinitionVO vo = taskDefinitionService.convertToVO(taskDefinition);

        assertTrue(vo.getId() > 0);
        assertThat(vo.getName(), is(taskDefinition.getName()));
        assertThat(vo.getCreateTime(), is(taskDefinition.getCreateTime()));
        assertThat(vo.getLastModifier(), is(taskDefinition.getLastModifier()));
        assertThat(vo.getLastUpdateTime(), is(taskDefinition.getUpdateTime()));
        assertThat(vo.getOwner(), is(taskDefinition.getOwner()));
        assertThat(vo.getTaskTemplateName(), is(taskDefinition.getTaskTemplateName()));
        assertThat(vo.getTaskPayload(), sameBeanAs(taskDefinition.getTaskPayload()));
        assertThat(vo.getCreator(), is(taskDefinition.getCreator()));
        assertThat(vo.isArchived(), is(taskDefinition.isArchived()));
        assertThat(vo.isDeployed(), is(false));
        assertThat(vo.isUpdated(), is(false));
        assertThat(vo.getTaskCommits().size(), is(1));
        // upstreams
        assertThat(vo.getUpstreamTaskDefinitions().size(), is(1));
        assertThat(vo.getUpstreamTaskDefinitions().get(0).getId(), is(upstreamTaskDefinition.getDefinitionId()));
        assertThat(vo.getUpstreamTaskDefinitions().get(0).getName(), is(upstreamTaskDefinition.getName()));
    }

    @Test
    public void test_convertToListVO() {
        // create definition with dependencies
        TaskDefinition upstreamTaskDefinition = MockTaskDefinitionFactory.createTaskDefinition();
        taskDefinitionDao.create(upstreamTaskDefinition);
        TaskDefinition taskDefinition = MockTaskDefinitionFactory.createTaskDefinitions(1, Collections.singletonList(upstreamTaskDefinition.getDefinitionId()))
                .get(0);
        taskDefinitionDao.create(taskDefinition);

        List<TaskDefinitionVO> taskDefinitionVOS = taskDefinitionService.convertToVOList(Arrays.asList(upstreamTaskDefinition, taskDefinition), true);
        TaskDefinitionVO vo = taskDefinitionVOS.get(1);

        assertTrue(vo.getId() > 0);
        assertThat(vo.getName(), is(taskDefinition.getName()));
        assertThat(vo.getCreateTime(), is(taskDefinition.getCreateTime()));
        assertThat(vo.getLastModifier(), is(taskDefinition.getLastModifier()));
        assertThat(vo.getLastUpdateTime(), is(taskDefinition.getUpdateTime()));
        assertThat(vo.getOwner(), is(taskDefinition.getOwner()));
        assertThat(vo.getTaskTemplateName(), is(taskDefinition.getTaskTemplateName()));
        assertThat(vo.getTaskPayload(), sameBeanAs(taskDefinition.getTaskPayload()));
        assertThat(vo.getCreator(), is(taskDefinition.getCreator()));
        assertThat(vo.isArchived(), is(taskDefinition.isArchived()));
        assertThat(vo.isDeployed(), is(false));
        assertThat(vo.getTaskCommits(), is(empty()));
        // upstreams name should be empty
        assertThat(vo.getUpstreamTaskDefinitions().size(), is(1));
        assertThat(vo.getUpstreamTaskDefinitions().get(0).getId(), is(upstreamTaskDefinition.getDefinitionId()));
        assertThat(vo.getUpstreamTaskDefinitions().get(0).getName(), is(""));
    }

    @Disabled
    @Test
    public void test_delete() {
        // prepare
        TaskDefinition taskDefinition = MockTaskDefinitionFactory.createTaskDefinition();
        taskDefinitionDao.create(taskDefinition);
        Long defId = taskDefinition.getDefinitionId();

        String msg = "Create test message";
        CommitRequest request = new CommitRequest(defId, msg);

        // invocation
        deployService.deployFast(taskDefinition.getDefinitionId(), request);
        taskDefinitionService.delete(taskDefinition.getDefinitionId());

        Optional<TaskDefinition> definitionOp = taskDefinitionDao.fetchById(defId);
        assertThat(definitionOp.isPresent(), is(true));
        TaskDefinition def = definitionOp.get();
        assertThat(def.isArchived(), is(true));

    }


    @Disabled
    @Test
    public void test_delete_with_downstream_dependency() {
        // if task has downstream dependencies, fail to delete

        // prepare with dependencies
        TaskDefinition upstreamTaskDefinition = MockTaskDefinitionFactory.createTaskDefinition();
        taskDefinitionDao.create(upstreamTaskDefinition);

        TaskDefinition taskDefinition = MockTaskDefinitionFactory.createTaskDefinition();
        taskDefinitionDao.create(taskDefinition);
        TaskPayload taskPayload = taskDefinition.getTaskPayload();
        Map<String, Object> taskConfig = taskPayload.getTaskConfig();
        taskConfig.put("sql", "select 2");
        ScheduleConfig scheduleConfig = ScheduleConfig.newBuilder()
                .withInputNodes(Collections.singletonList(upstreamTaskDefinition.getDefinitionId()))
                .build();
        TaskPayload updatedTaskPayload = taskPayload
                .cloneBuilder()
                .withTaskConfig(taskConfig)
                .withScheduleConfig(scheduleConfig)
                .build();

        UpdateTaskDefinitionRequest updateRequest = new UpdateTaskDefinitionRequest(
                taskDefinition.getDefinitionId(),
                taskDefinition.getName() + "_updated",
                updatedTaskPayload,
                1L
        );

        TaskDefinition updated = taskDefinitionService.update(updateRequest.getDefinitionId(), updateRequest);

        // delete upstream task should fail
        RuntimeException exception = null;
        try {
            taskDefinitionService.delete(upstreamTaskDefinition.getDefinitionId());
        } catch (RuntimeException e) {
            exception = e;
        }
        assertNotNull(exception);
        assertTrue(exception.getMessage().startsWith("Task definition has downStream dependencies"));

        // delete downstream first, then delete upstream
        taskDefinitionService.delete(taskDefinition.getDefinitionId());
        Optional<TaskDefinition> downstreamTask = taskDefinitionDao.fetchById(taskDefinition.getDefinitionId());
        assertTrue(downstreamTask.get().isArchived());

        List<TaskRelation> taskRelations = taskRelationDao.fetchByDownstreamId(taskDefinition.getDefinitionId());
        assertTrue(taskRelations.isEmpty());

        taskDefinitionService.delete(upstreamTaskDefinition.getDefinitionId());
        Optional<TaskDefinition> upstreamTask = taskDefinitionDao.fetchById(taskDefinition.getDefinitionId());
        assertTrue(upstreamTask.get().isArchived());
    }

    @Test
    public void test_deploy_fail_when_upstream_not_deployed() {
        // task relation should be removed as well when delete task
        //need mock workflow
    }


    //Scenario: only one taskDef in batch
    @Test
    public void runBatchOneTaskDef_shouldSuccess() {
        TaskDefinition taskDef = MockTaskDefinitionFactory.createTaskDefinition();
        taskDefinitionDao.create(taskDef);
        TaskTryBatchRequest taskTryBatchRequest = new TaskTryBatchRequest(Collections.singletonList(taskDef.getDefinitionId()));

        List<Task> taskList = new ArrayList<>();
        Mockito.doAnswer(invocation -> {
            Task task = invocation.getArgument(0);
            task = task.cloneBuilder()
                    .withId(WorkflowIdGenerator.nextTaskId())
                    .build();
            taskList.add(task);
            return task;
        }).when(workflowClient).saveTask(any(Task.class), any(List.class));

        Mockito.doAnswer(invocation -> MockTaskRunFactory.createTaskRuns(taskList)).when(workflowClient).executeTasks(any(RunTaskRequest.class));

        List<TaskTry> taskTryList = taskDefinitionService.runBatch(taskTryBatchRequest);

        assertThat(taskTryList.size(), is(1));
        TaskTry taskTry = taskTryList.get(0);
        assertThat(taskTry.getDefinitionId(), is(taskDef.getDefinitionId()));
    }

    //scenario
    //  taskDef  1,2 -> 3
    // select 1 2 3 to try run
    @Test
    public void runBatchMultiTaskDef_shouldSuccess() {
        TaskDefinition taskDef1 = MockTaskDefinitionFactory.createTaskDefinition();
        TaskDefinition taskDef2 = MockTaskDefinitionFactory.createTaskDefinition();
        TaskDefinition taskDef3 = MockTaskDefinitionFactory.createTaskDefinitions(1,
                Arrays.asList(taskDef1.getDefinitionId(), taskDef2.getDefinitionId())).get(0);
        taskDefinitionDao.create(taskDef1);
        taskDefinitionDao.create(taskDef2);
        taskDefinitionDao.create(taskDef3);

        TaskTryBatchRequest taskTryBatchRequest = new TaskTryBatchRequest(Arrays.asList(taskDef1.getDefinitionId(),
                taskDef2.getDefinitionId(), taskDef3.getDefinitionId()));

        List<Task> taskList = new ArrayList<>();
        Mockito.doAnswer(invocation -> {
            Task task = invocation.getArgument(0);
            task = task.cloneBuilder()
                    .withId(WorkflowIdGenerator.nextTaskId())
                    .build();
            taskList.add(task);
            return task;
        }).when(workflowClient).saveTask(any(Task.class), any(List.class));

        Mockito.doAnswer(invocation -> MockTaskRunFactory.createTaskRuns(taskList)).when(workflowClient).executeTasks(any(RunTaskRequest.class));

        List<TaskTry> taskTryList = taskDefinitionService.runBatch(taskTryBatchRequest);

        assertThat(taskTryList.size(), is(3));
        assertThat(new HashSet<>(taskTryList.stream().map(TaskTry::getDefinitionId).collect(Collectors.toList())),
                is(new HashSet<>(Arrays.asList(taskDef1.getDefinitionId(), taskDef2.getDefinitionId(), taskDef3.getDefinitionId()))));

    }

    //scenario
    //taskDef  1 -> 2,  2 -> 1
    //check circular dependency
    @Test
    public void runMultiTaskDefBatch_withCircularDependency_shouldFail() {
        Long taskDef1Id = DataPlatformIdGenerator.nextDefinitionId();
        TaskDefinition taskDef2 = MockTaskDefinitionFactory.createTaskDefinitions(1, Collections.singletonList(taskDef1Id)).get(0);
        TaskDefinition taskDef1 = MockTaskDefinitionFactory.createTaskDefinitions(1, Collections.singletonList(taskDef2.getDefinitionId()), taskDef1Id).get(0);
        taskDefinitionDao.create(taskDef1);
        taskDefinitionDao.create(taskDef2);
        TaskTryBatchRequest taskTryBatchRequest = new TaskTryBatchRequest(Arrays.asList(taskDef1.getDefinitionId(),
                taskDef2.getDefinitionId()));
        Exception ex = assertThrows(RuntimeException.class, () -> taskDefinitionService.runBatch(taskTryBatchRequest));
        assertEquals("Task try run batch has circular dependence", ex.getMessage());
    }

    //scenario:
    // taskDef  1,2 -> 3, 3,4 -> 5
    // select 1 3 5 to try run
    @Test
    public void runBatchMultiTaskDef_selectedTaskDef_shouldSuccess() {
        TaskDefinition taskDef1 = MockTaskDefinitionFactory.createTaskDefinition();
        TaskDefinition taskDef2 = MockTaskDefinitionFactory.createTaskDefinition();
        TaskDefinition taskDef3 = MockTaskDefinitionFactory.createTaskDefinitions(1,
                Arrays.asList(taskDef1.getDefinitionId(), taskDef2.getDefinitionId())).get(0);
        TaskDefinition taskDef4 = MockTaskDefinitionFactory.createTaskDefinition();
        TaskDefinition taskDef5 = MockTaskDefinitionFactory.createTaskDefinitions(1,
                Arrays.asList(taskDef3.getDefinitionId(), taskDef4.getDefinitionId())).get(0);
        taskDefinitionDao.create(taskDef1);
        taskDefinitionDao.create(taskDef2);
        taskDefinitionDao.create(taskDef3);
        taskDefinitionDao.create(taskDef4);
        taskDefinitionDao.create(taskDef5);

        List<Long> tryTaskDefIdList = Arrays.asList(taskDef1.getDefinitionId(), taskDef3.getDefinitionId(), taskDef5.getDefinitionId());
        TaskTryBatchRequest taskTryBatchRequest = new TaskTryBatchRequest(tryTaskDefIdList);

        List<Task> taskList = new ArrayList<>();
        Mockito.doAnswer(invocation -> {
            Task task = invocation.getArgument(0);
            task = task.cloneBuilder()
                    .withId(WorkflowIdGenerator.nextTaskId())
                    .build();
            taskList.add(task);
            return task;
        }).when(workflowClient).saveTask(any(Task.class), any(List.class));

        Mockito.doAnswer(invocation -> MockTaskRunFactory.createTaskRuns(taskList)).when(workflowClient).executeTasks(any(RunTaskRequest.class));

        List<TaskTry> taskTryList = taskDefinitionService.runBatch(taskTryBatchRequest);

        assertThat(taskTryList.size(), is(3));
        assertThat(new HashSet<>(taskTryList.stream().map(TaskTry::getDefinitionId).collect(Collectors.toList())),
                is(new HashSet<>(Arrays.asList(taskDef1.getDefinitionId(), taskDef3.getDefinitionId(), taskDef5.getDefinitionId()))));
    }

    //Scenario:
    // taskdef 1 -> 2
    // run batch and stop them
    @Test
    public void stopBatch_shouldSuccess() {
        //prepare
        TaskDefinition taskDef1 = MockTaskDefinitionFactory.createTaskDefinition();
        TaskDefinition taskDef2 = MockTaskDefinitionFactory.createTaskDefinitions(1,
                Collections.singletonList(taskDef1.getDefinitionId())).get(0);
        taskDefinitionDao.create(taskDef1);
        taskDefinitionDao.create(taskDef2);

        TaskTryBatchRequest taskTryBatchRequest1 = new TaskTryBatchRequest(Arrays.asList(taskDef1.getDefinitionId(),
                taskDef2.getDefinitionId()));

        List<Task> taskList = new ArrayList<>();
        Mockito.doAnswer(invocation -> {
            Task task = invocation.getArgument(0);
            task = task.cloneBuilder()
                    .withId(WorkflowIdGenerator.nextTaskId())
                    .build();
            taskList.add(task);
            return task;
        }).when(workflowClient).saveTask(any(Task.class), any(List.class));

        Mockito.doAnswer(invocation -> MockTaskRunFactory.createTaskRuns(taskList)).when(workflowClient).executeTasks(any(RunTaskRequest.class));

        //run batch
        List<TaskTry> taskTryList = taskDefinitionService.runBatch(taskTryBatchRequest1);

        assertThat(taskTryList.size(), is(2));
        assertThat(new HashSet<>(taskTryList.stream().map(TaskTry::getDefinitionId).collect(Collectors.toList())),
                is(new HashSet<>(Arrays.asList(taskDef1.getDefinitionId(), taskDef2.getDefinitionId()))));

        //stop batch
        Mockito.doNothing().when(workflowClient).stopTaskRuns(any(List.class));
        TaskTryBatchRequest taskTryBatchRequest2 = new TaskTryBatchRequest(taskTryList.stream().map(TaskTry::getId).collect(Collectors.toList()));
        taskDefinitionService.stopBatch(taskTryBatchRequest2);
        Mockito.verify(workflowClient, Mockito.times(1)).stopTaskRuns(any(List.class));
    }

}