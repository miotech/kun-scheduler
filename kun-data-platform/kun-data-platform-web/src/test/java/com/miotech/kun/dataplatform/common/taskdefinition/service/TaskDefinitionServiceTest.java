package com.miotech.kun.dataplatform.common.taskdefinition.service;

import com.miotech.kun.dataplatform.AppTestBase;
import com.miotech.kun.dataplatform.common.commit.vo.CommitRequest;
import com.miotech.kun.dataplatform.common.deploy.service.DeployService;
import com.miotech.kun.dataplatform.common.taskdefinition.dao.TaskDefinitionDao;
import com.miotech.kun.dataplatform.common.taskdefinition.dao.TaskRelationDao;
import com.miotech.kun.dataplatform.common.taskdefinition.vo.*;
import com.miotech.kun.dataplatform.mocking.MockTaskDefinitionFactory;
import com.miotech.kun.dataplatform.model.deploy.Deploy;
import com.miotech.kun.dataplatform.model.deploy.DeployCommit;
import com.miotech.kun.dataplatform.model.deploy.DeployStatus;
import com.miotech.kun.dataplatform.model.taskdefinition.*;
import com.miotech.kun.security.testing.WithMockTestUser;
import com.miotech.kun.workflow.client.WorkflowClient;
import com.miotech.kun.workflow.client.model.TaskRun;
import com.miotech.kun.workflow.client.model.TaskRunLogRequest;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunStatus;
import org.json.simple.JSONObject;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.miotech.kun.dataplatform.common.tasktemplate.dao.TaskTemplateDaoTest.TEST_TEMPLATE;
import static com.miotech.kun.workflow.core.model.taskrun.TaskRunStatus.*;
import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.in;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

// TODO: figure out a solution to bootstrap Workflow facade related tests
@Ignore
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

    @Test
    public void find() {
    }

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
    public void test_update_withInputNodes() {
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
        assertThat(vo.getStatus(), in(new TaskRunStatus[]{RUNNING,SUCCESS,FAILED}));
        assertThat(vo.getTaskRunId(), is(taskTry.getWorkflowTaskRunId()));
        assertTrue(vo.getLogs().size() > 0);
    }

    @Test
    public void test_convertToVO() {
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
        // upstreams name should be empty
        assertThat(vo.getUpstreamTaskDefinitions().size(), is(1));
        assertThat(vo.getUpstreamTaskDefinitions().get(0).getId(), is(upstreamTaskDefinition.getDefinitionId()));
        assertThat(vo.getUpstreamTaskDefinitions().get(0).getName(), is(""));
    }

    @Test
    public void test_delete(){
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

    @Test
    public void test_delete_with_downstream_dependency(){
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
        try{
            taskDefinitionService.delete(upstreamTaskDefinition.getDefinitionId());
        }catch (RuntimeException e){
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
    public void test_deploy_fail_when_upstream_not_deployed(){
        // task relation should be removed as well when delete task
        //need mock workflow
    }
}