package com.miotech.kun.workflow.web.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.miotech.kun.workflow.common.exception.EntityNotFoundException;
import com.miotech.kun.workflow.common.exception.ExceptionResponse;
import com.miotech.kun.workflow.common.task.filter.TaskSearchFilter;
import com.miotech.kun.workflow.common.task.service.TaskService;
import com.miotech.kun.workflow.common.task.vo.TaskPropsVO;
import com.miotech.kun.workflow.core.model.common.Tag;
import com.miotech.kun.workflow.core.model.operator.Operator;
import com.miotech.kun.workflow.core.model.task.Task;
import com.miotech.kun.workflow.testing.factory.MockOperatorFactory;
import com.miotech.kun.workflow.testing.factory.MockTaskFactory;
import com.miotech.kun.workflow.utils.WorkflowIdGenerator;
import com.miotech.kun.workflow.web.KunWebServerTestBase;
import com.miotech.kun.workflow.web.entity.AcknowledgementVO;
import com.miotech.kun.workflow.common.task.vo.PaginationVO;
import com.miotech.kun.workflow.web.serializer.JsonSerializer;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.mockito.internal.matchers.Null;
import org.mockito.stubbing.Answer;

import java.util.*;
import java.util.stream.Collectors;

import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.argThat;

public class TaskControllerTest extends KunWebServerTestBase {

    private static final List<Operator> mockOperatorList = new ArrayList<>();
    private static List<Task> mockTaskList = Collections.synchronizedList(new ArrayList<>());
    private static final TypeReference<PaginationVO<Task>> taskPaginationVOTypeRef = new TypeReference<PaginationVO<Task>>() {};

    static {
        // Create 10 mock operators as stored
        for (int i = 0; i < 10; ++i) {
            mockOperatorList.add(MockOperatorFactory.createOperator());
        }
        // Create 200 tasks as stored
        List<Task> tasks = MockTaskFactory.createTasks(200);
        for (int i = 0; i < tasks.size(); ++i) {
            List<Tag> tags = (i % 20 == 0) ?
                    Lists.newArrayList(new Tag("version", "1.1")) :
                    Lists.newArrayList(new Tag("version", "1.0"));
            tasks.set(i, tasks.get(i).cloneBuilder()
                    .withOperatorId(mockOperatorList.get(i % 10).getId())
                    .withTags(tags)
                    .build()
            );
        }
        mockTaskList.addAll(tasks);
    }

    @Inject
    private JsonSerializer jsonSerializer;

    private final TaskService taskService = mock(TaskService.class);

    private static final class IsEmptyTaskSearchFilter implements ArgumentMatcher<TaskSearchFilter> {
        @Override
        public boolean matches(TaskSearchFilter argument) {
            return Objects.nonNull(argument) &&
                    StringUtils.isBlank(argument.getName()) &&
                    (Objects.isNull(argument.getTags()) || argument.getTags().isEmpty()) &&
                    (Objects.isNull(argument.getPageNum())) &&
                    (Objects.isNull(argument.getPageSize()));
        }
    }

    private static final class IsValidSearchFilter implements ArgumentMatcher<TaskSearchFilter> {
        @Override
        public boolean matches(TaskSearchFilter argument) {
            return Objects.nonNull(argument) &&
                    (Objects.nonNull(argument.getPageNum())) &&
                    (Objects.nonNull(argument.getPageSize()));
        }
    }

    private static List<Task> mockSearchTasks(TaskSearchFilter filter, boolean usePagination) {
        List<Task> matchedResults = mockTaskList.stream()
                // filter by name
                .filter(task -> {
                    if (StringUtils.isNotEmpty(filter.getName())) {
                        return StringUtils.contains(task.getName(), filter.getName());
                    }
                    // else
                    return true;
                })
                // filter by tags
                .filter(task -> {
                    if (Objects.isNull(filter.getTags()) || (filter.getTags().isEmpty())) {
                        return true;
                    }
                    // else
                    List<Tag> tags = filter.getTags();
                    if (task.getTags().size() != tags.size()) {
                        return false;
                    }
                    for (int i = 0; i < tags.size(); ++i) {
                        if (!Objects.equals(task.getTags().get(i), tags.get(i))) {
                            return false;
                        }
                    }
                    return true;
                })
                .collect(Collectors.toList());
        int startIndex = (filter.getPageNum() - 1) * filter.getPageSize();
        int endIndex = Math.min(filter.getPageNum() * filter.getPageSize(), matchedResults.size());
        return usePagination ? matchedResults.subList(startIndex, endIndex) : matchedResults;
    }

    private void mockFetchTasksByFiltersBehavior() {
        // Throw exception when task search filter is null (not expected to happen)
        Mockito.doThrow(new NullPointerException())
                .when(taskService)
                .fetchTasksByFilters((TaskSearchFilter) argThat(Null.NULL));
        // For paginated search filters, return expected range
        Mockito.doAnswer((Answer<PaginationVO<Task>>) invocation -> {
            TaskSearchFilter filter = invocation.getArgument(0);
            List<Task> matchedTasks = mockSearchTasks(filter, true);
            int count = mockSearchTasks(filter, false).size();
            return PaginationVO.<Task>newBuilder()
                    .withRecords(matchedTasks)
                    .withPageNumber(filter.getPageNum())
                    .withPageSize(filter.getPageSize())
                    .withTotalCount(count).build();
        })
                .when(taskService)
                .fetchTasksByFilters(argThat(new IsValidSearchFilter()));

        Mockito.doAnswer((Answer<Integer>) invocation -> {
            TaskSearchFilter filter = invocation.getArgument(0);
            return mockSearchTasks(filter, false).size();
        })
                .when(taskService)
                .fetchTaskTotalCount(argThat(new IsValidSearchFilter()));
    }

    private void mockFetchTasksByIdBehavior() {
        Mockito.doAnswer(invocation -> {
            long taskId = invocation.getArgument(0);
            for (Task mockTask : mockTaskList) {
                if (Objects.equals(taskId, mockTask.getId())) {
                    return mockTask;
                }
            }
            // else
            throw new EntityNotFoundException(String.format("Cannot find task with id: %s", taskId));
        }).when(taskService).find(anyLong());
    }

    private void mockUpdateBehavior() {
        Mockito.doAnswer(invocation -> {
            TaskPropsVO vo = invocation.getArgument(0);
            Preconditions.checkArgument(vo.getName() != null, "Invalid task property \"name\"");

            Task task = Task.newBuilder()
                    .withId(WorkflowIdGenerator.nextTaskId())
                    .withName(vo.getName())
                    .withDescription(vo.getDescription())
                    .withConfig(vo.getConfig())
                    .withOperatorId(vo.getOperatorId())
                    .withDependencies(vo.getDependencies())
                    .withTags(vo.getTags())
                    .withScheduleConf(vo.getScheduleConf())
                    .build();
            mockTaskList.add(task);
            return task;
        }).when(taskService).createTask(ArgumentMatchers.any(TaskPropsVO.class));

        Mockito.doAnswer(invocation -> {
            long taskId = invocation.getArgument(0);
            if (mockTaskList.stream().noneMatch(task -> task.getId() == taskId)) {
                throw new EntityNotFoundException(String.format("Cannot delete non-exist task with id: %d", taskId));
            }
            mockTaskList = mockTaskList.stream().filter(task -> task.getId() != taskId).collect(Collectors.toList());
            // else
            return new AcknowledgementVO("Delete success");
        }).when(taskService).deleteTaskById(anyLong());
    }

    @Before
    public void defineBehaviors() {
        // mock method behaviors of task service
        // 1. Mock method: taskService.fetchTasksByFilters
        mockFetchTasksByFiltersBehavior();
        // 2. Mock method: taskService.fetchTaskById
        mockFetchTasksByIdBehavior();
        // 3. Mock method: taskService.createTask
        mockUpdateBehavior();
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
    public void fetchTasksByFilters_withComplexFilter_shouldReturn() {
        String nameFilterResponse = post("/tasks/_search", "{\"name\": \"task_\"}");
        String randomNameFilterResponse = post("/tasks/_search", "{\"name\": \"random_string_search\"}");
        String tagsFilterResponse = post("/tasks/_search", "{\"tags\": [{ \"key\": \"version\", \"value\": \"1.1\" }]}");

        PaginationVO<Task> nameFilterResponseData = jsonSerializer.toObject(nameFilterResponse, taskPaginationVOTypeRef);
        PaginationVO<Task> randomNameFilterResponseData = jsonSerializer.toObject(randomNameFilterResponse, taskPaginationVOTypeRef);
        PaginationVO<Task> tagsFilterResponseData = jsonSerializer.toObject(tagsFilterResponse, taskPaginationVOTypeRef);

        assertThat(nameFilterResponseData.getRecords().size(), is(100));
        assertThat(randomNameFilterResponseData.getRecords().size(), is(0));
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
    public void createTask_withValidTaskPropsValueObject_shouldWork() {
        int mockTaskListSize = mockTaskList.size();
        String postJson = "{\"name\":\"scheduled_test_task\"," +
                "\"description\":\"scheduled_test_task description\"," +
                String.format("\"operatorId\":\"%s\",", mockOperatorList.get(0).getId()) +
                "\"arguments\":[]," +
                "\"variableDefs\":[]," +
                "\"scheduleConf\":{\"type\":\"SCHEDULED\",\"cronExpr\":\"*/1 * * * * ?\"}," +
                "\"dependencies\":[]," +
                "\"tags\": [{ \"key\": \"version\", \"value\": \"1.2\" }]}";

        String response = post("/tasks", postJson);
        assertThat(mockTaskList.size(), is(mockTaskListSize + 1));

        // Validate
        Task responseTask = jsonSerializer.toObject(response, Task.class);
        assertNotNull(responseTask.getId());

        // Remove after test
        mockTaskList.remove(mockTaskListSize);
    }

    @Test
    public void createTask_withInValidTaskPropsValueObject_shouldResponseBadRequestException() {
        String response = post("/tasks", "{\"prop\":\"anything\"}");
        ExceptionResponse exceptionResponse = jsonSerializer.toObject(response, ExceptionResponse.class);
        assertThat(exceptionResponse.getCode(), is(400));
    }

    @Test
    public void deleteTask_withExistingTaskId_shouldRemoveTaskAndResponse() {
        int mockTaskListSize = mockTaskList.size();
        Task taskTemporaryRemoved = mockTaskList.get(mockTaskListSize - 1).cloneBuilder().build();

        String response = delete(String.format("/tasks/%s", mockTaskList.get(mockTaskListSize - 1).getId()));

        assertThat(mockTaskList.size(), is(mockTaskListSize - 1));
        assertThat(response, is("{\"ack\":true,\"message\":\"Delete success\"}"));

        // Push task instance back after test
        mockTaskList.add(taskTemporaryRemoved);
    }

    @Test
    public void deleteTask_withNonExistingTaskId_shouldResponseNotFound() {
        String response = delete("/tasks/1234567");
        ExceptionResponse exceptionResponse = jsonSerializer.toObject(response, ExceptionResponse.class);
        assertThat(exceptionResponse.getCode(), is(404));
    }
}
