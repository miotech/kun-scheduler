package com.miotech.kun.workflow.web.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.miotech.kun.commons.db.DatabaseOperator;
import com.miotech.kun.commons.web.serializer.JsonSerializer;
import com.miotech.kun.workflow.common.exception.ExceptionResponse;
import com.miotech.kun.workflow.common.operator.dao.OperatorDao;
import com.miotech.kun.workflow.common.task.dao.TaskDao;
import com.miotech.kun.workflow.common.task.vo.PaginationVO;
import com.miotech.kun.workflow.common.task.vo.RunTaskVO;
import com.miotech.kun.workflow.core.execution.Config;
import com.miotech.kun.workflow.core.model.common.Tag;
import com.miotech.kun.workflow.core.model.operator.Operator;
import com.miotech.kun.workflow.core.model.task.Task;
import com.miotech.kun.workflow.testing.factory.MockOperatorFactory;
import com.miotech.kun.workflow.testing.factory.MockTaskFactory;
import com.miotech.kun.workflow.testing.operator.OperatorCompiler;
import com.miotech.kun.workflow.utils.JSONUtils;
import com.miotech.kun.workflow.web.KunWebServerTestBase;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

public class TaskControllerTest extends KunWebServerTestBase {

    private static final TypeReference<PaginationVO<Task>> taskPaginationVOTypeRef = new TypeReference<PaginationVO<Task>>() {
    };
    private List<Task> mockTaskList = new ArrayList<>();

    @Inject
    private JsonSerializer jsonSerializer;

    @Inject
    private DatabaseOperator dbOperator;

    @Inject
    private TaskDao taskDao;

    @Inject
    private OperatorDao operatorDao;

    @Before
    public void preapreData() {
        String generatedString = RandomStringUtils.randomAlphabetic(10);
        Operator operator = MockOperatorFactory.createOperator()
                .cloneBuilder()
                .withName(generatedString)
                .build();
        operatorDao.create(operator);
        mockTaskList = new ArrayList<>();
        for (int i = 0; i < 200; ++i) {

            Task task = MockTaskFactory.createTask();
            List<Tag> tags = (i % 20 == 0) ?
                    Lists.newArrayList(new Tag("version", "1.1")) :
                    Lists.newArrayList(new Tag("version", "1.0"));
            mockTaskList.add(task.cloneBuilder()
                    .withOperatorId(operator.getId())
                    .withTags(tags).build());
        }

        dbOperator.update("TRUNCATE TABLE kun_wf_task");
        mockTaskList.forEach(taskDao::create);
    }

    // Test cases begin

    @Test
    public void fetchTasksByFilters_withEmptyFilter_shouldReturnAtMost100ResultsPerPage() {
        String response = get("/tasks");
        PaginationVO<Task> responseData = jsonSerializer.toObject(response, taskPaginationVOTypeRef);
        assertThat(responseData.getRecords().size(), is(100));
        assertThat(responseData.getTotalCount(), is(200));
    }

    @Test
    public void fetchTasksByFilters_withPaginatedFilter_shouldReturnPageProperly() {
        String response = get("/tasks?pageNum=2&pageSize=50");
        String lastPageResponse = get("/tasks?pageNum=3&pageSize=80");
        PaginationVO<Task> responseData = jsonSerializer.toObject(response, taskPaginationVOTypeRef);
        PaginationVO<Task> lastPageResponseData = jsonSerializer.toObject(lastPageResponse, taskPaginationVOTypeRef);

        assertThat(responseData.getRecords().size(), is(50));
        assertThat(responseData.getTotalCount(), is(200));
        assertThat(lastPageResponseData.getRecords().size(), is(40));  // 200 % 80 = 40
        assertThat(lastPageResponseData.getTotalCount(), is(200));
    }

    @Test
    public void fetchTasksByFilters_withName_shouldReturn() {
        String nameFilterResponse = post("/tasks/_search", "{\"name\": \"task_\"}");
        PaginationVO<Task> nameFilterResponseData = jsonSerializer.toObject(nameFilterResponse, taskPaginationVOTypeRef);
        assertThat(nameFilterResponseData.getRecords().size(), is(100));

        String randomNameFilterResponse = post("/tasks/_search", "{\"name\": \"random_string_search\"}");
        PaginationVO<Task> randomNameFilterResponseData = jsonSerializer.toObject(randomNameFilterResponse, taskPaginationVOTypeRef);
        assertThat(randomNameFilterResponseData.getRecords().size(), is(0));
    }

    @Test
    public void fetchTasksByFilters_withTags_shouldReturn() {
        String tagsFilterResponse = post("/tasks/_search", "{\"tags\": [{ \"key\": \"version\", \"value\": \"1.1\" }]}");
        PaginationVO<Task> tagsFilterResponseData = jsonSerializer.toObject(tagsFilterResponse, taskPaginationVOTypeRef);
        assertThat(tagsFilterResponseData.getRecords().size(), is(10));
    }

    @Test
    public void fetchTaskById_withExistingId_shouldResponseTargetTask() {
        Task exampleTask = mockTaskList.get(0);
        String response = get(String.format("/tasks/%s", exampleTask.getId()));

        Task responseTask = jsonSerializer.toObject(response, Task.class);
        assertThat(responseTask, sameBeanAs(exampleTask));
    }

    @Test
    public void fetchTaskById_withNonExistingId_shouldResponseNotFound() {
        String response = get("/tasks/1234567");

        ExceptionResponse exceptionResponse = jsonSerializer.toObject(response, ExceptionResponse.class);
        assertThat(exceptionResponse.getCode(), is(404));
        assertThat(exceptionResponse.getMessage(), is("Cannot find task with id: 1234567"));
    }

    @Test
    public void createTask_and_update_shouldWork() {
        Task upstreamTask = mockTaskList.get(0);
        // CREATE
        String postJson = String.format("{\"name\":\"scheduled_test_task\"," +
                "\"description\":\"scheduled_test_task description\"," +
                "\"operatorId\":\"%s\"," +
                "\"config\":{\"values\": {}}," +
                "\"dependencies\":[]," +
                "\"scheduleConf\":{\"type\":\"SCHEDULED\",\"cronExpr\":\"*/1 * * * * ?\"}," +
                "\"tags\": [{ \"key\": \"version\", \"value\": \"1.2\" }]}", upstreamTask.getOperatorId());

        String response = post("/tasks", postJson);

        // verify
        JsonNode result = JSONUtils.jsonToObject(response, JsonNode.class);
        Long taskID = Long.parseLong(result.get("id").asText());
        assertTrue(taskID > 0);
        assertThat(result.get("operatorId").asText(), is(upstreamTask.getOperatorId().toString()));
        assertThat(result.get("name").asText(), is("scheduled_test_task"));
        assertThat(result.get("description").asText(), is("scheduled_test_task description"));
        assertThat(result.get("config").toString(), is("{\"values\":{}}"));
        assertThat(result.get("scheduleConf").toString(), is("{\"type\":\"SCHEDULED\",\"cronExpr\":\"*/1 * * * * ?\"}"));
        assertThat(result.get("dependencies").toString(), is("[]"));
        assertThat(result.get("tags").toString(), is("[{\"key\":\"version\",\"value\":\"1.2\"}]"));

        // update
        String putJson = String.format("{\"id\": \"%s\", \"name\":\"scheduled_test_task\"," +
                "\"description\":\"scheduled_test_task description UPDATED\"," +
                "\"operatorId\":\"%s\"," +
                "\"config\":{\"values\": {}}," +
                "\"dependencies\":[]," +
                "\"scheduleConf\":{\"type\":\"SCHEDULED\",\"cronExpr\":\"*/1 * * * * ?\"}," +
                "\"tags\": []}", taskID, upstreamTask.getOperatorId());

        response = put("/tasks/" + result.get("id").asText(), putJson);

        // verify
        result = JSONUtils.jsonToObject(response, JsonNode.class);
        assertThat(Long.parseLong(result.get("id").asText()), is(taskID));
        assertThat(result.get("operatorId").asText(), is(upstreamTask.getOperatorId().toString()));
        assertThat(result.get("name").asText(), is("scheduled_test_task"));
        assertThat(result.get("description").asText(), is("scheduled_test_task description UPDATED"));
        assertThat(result.get("config").toString(), is("{\"values\":{}}"));
        assertThat(result.get("scheduleConf").toString(), is("{\"type\":\"SCHEDULED\",\"cronExpr\":\"*/1 * * * * ?\"}"));
        assertThat(result.get("dependencies").toString(), is("[]"));
        assertThat(result.get("tags").toString(), is("[]"));
    }

    @Test
    public void createTask_withDependencies_shouldWork() {
        Task upstreamTask = mockTaskList.get(0);
        String postJson = String.format("{\"name\":\"scheduled_test_task\"," +
                "\"description\":\"scheduled_test_task description\"," +
                "\"operatorId\":\"%s\"," +
                "\"config\":{\"values\": {}}," +
                "\"scheduleConf\":{\"type\":\"SCHEDULED\",\"cronExpr\":\"*/1 * * * * ?\"}," +
                "\"dependencies\":[{\"upstreamTaskId\": \"%s\", \"dependencyFunc\": \"latestTaskRun\"}]," +
                "\"tags\": [{ \"key\": \"version\", \"value\": \"1.2\" }]}", upstreamTask.getOperatorId(), upstreamTask.getId());

        String response = post("/tasks", postJson);

        JsonNode result = JSONUtils.jsonToObject(response, JsonNode.class);
        assertTrue(Long.parseLong(result.get("id").asText()) > 0);
        assertThat(result.get("operatorId").asText(), is(upstreamTask.getOperatorId().toString()));
        assertThat(result.get("name").asText(), is("scheduled_test_task"));
        assertThat(result.get("description").asText(), is("scheduled_test_task description"));
        assertThat(result.get("config").toString(), is("{\"values\":{}}"));
        assertThat(result.get("scheduleConf").toString(), is("{\"type\":\"SCHEDULED\",\"cronExpr\":\"*/1 * * * * ?\"}"));
        assertThat(result.get("dependencies").toString(), is(String.format("[{\"upstreamTaskId\":\"%s\",\"downstreamTaskId\":null,\"dependencyFunc\":\"latestTaskRun\"}]", upstreamTask.getId())));
        assertThat(result.get("tags").toString(), is("[{\"key\":\"version\",\"value\":\"1.2\"}]"));
    }

    @Test
    public void createTask_withInValidTaskPropsValueObject_shouldResponseBadRequestException() {
        String response = post("/tasks", "{\"prop\":\"anything\"}");

        ExceptionResponse exceptionResponse = jsonSerializer.toObject(response, ExceptionResponse.class);
        assertThat(exceptionResponse.getCode(), is(400));
    }

    @Test
    public void createTaskWithInvalidConfig() {
        initOperator();
        Task task = MockTaskFactory.createTask(1l);
        String payload = JSONUtils.toJsonString(task);
        String response = post("/tasks", payload);
        ExceptionResponse exceptionResponse = jsonSerializer.toObject(response, ExceptionResponse.class);
        assertThat(exceptionResponse.getCode(), is(400));
        assertThat(exceptionResponse.getMessage(),is("Configuration var2 is required but not specified"));


    }

    @Test
    public void updateTaskWithInvalidConfig() {
        initOperator();
        Config config = Config.newBuilder()
                .addConfig("var2", "default2").build();
        Task task = MockTaskFactory.createTask(1l).cloneBuilder().withConfig(config).build();
        String payload = JSONUtils.toJsonString(task);
        String response = post("/tasks", payload);
        Task createdTask = JSONUtils.jsonToObject(response, Task.class);
        Config errorConfig = Config.newBuilder().addConfig("var3", "default3").build();
        Task updateTask = createdTask.cloneBuilder().withConfig(errorConfig).build();
        String updateRes = put("/tasks/" + updateTask.getId(), JSONUtils.toJsonString(updateTask));
        ExceptionResponse exceptionResponse = jsonSerializer.toObject(updateRes, ExceptionResponse.class);
        assertThat(exceptionResponse.getCode(), is(400));
        assertThat(exceptionResponse.getMessage(),is("Configuration var2 is required but not specified"));

    }

    @Test
    public void executeTaskWithInvalidConfig() {
        initOperator();
        Config config = Config.newBuilder()
                .addConfig("var2", "default2").build();
        Task task = MockTaskFactory.createTask(1l).cloneBuilder().withConfig(config).build();
        String payload = JSONUtils.toJsonString(task);
        String response = post("/tasks", payload);
        Task createdTask = JSONUtils.jsonToObject(response, Task.class);
        RunTaskVO taskVO = new RunTaskVO();
        Config errorConfig = Config.newBuilder().addConfig("var3", "default3").build();
        taskVO.setTaskId(createdTask.getId());
        taskVO.setConfig(errorConfig.getValues());
        String request = JSONUtils.toJsonString(Lists.newArrayList(taskVO));
        String runRes = post("/tasks/_run",request);
        ExceptionResponse exceptionResponse = jsonSerializer.toObject(runRes, ExceptionResponse.class);
        assertThat(exceptionResponse.getCode(),is(400));
        assertThat(exceptionResponse.getMessage(),is("Unknown configuration 'var3'"));


    }


    private void initOperator() {
        long operatorId = 1L;
        String className = "TestOperator1";

        String packagePath = OperatorCompiler.compileJar(TestOperator.class, className);
        Operator op = MockOperatorFactory.createOperator()
                .cloneBuilder()
                .withId(operatorId)
                .withName(className)
                .withClassName(className)
                .withPackagePath(packagePath)
                .build();
        operatorDao.createWithId(op, operatorId);
    }


    @Test
    public void deleteTask_withExistingTaskId_shouldRemoveTaskAndResponse() {
        int mockTaskListSize = mockTaskList.size();

        String response = delete(String.format("/tasks/%s", mockTaskList.get(mockTaskListSize - 1).getId()));

        assertThat(response, is("{\"ack\":true,\"message\":\"Delete success\"}"));
    }

    @Test
    public void deleteTask_withNonExistingTaskId_shouldResponseNotFound() {
        String response = delete("/tasks/1234567");
        ExceptionResponse exceptionResponse = jsonSerializer.toObject(response, ExceptionResponse.class);
        assertThat(exceptionResponse.getCode(), is(404));
    }
}
