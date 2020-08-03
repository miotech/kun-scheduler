package com.miotech.kun.workflow.web.controller;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.miotech.kun.workflow.common.task.filter.TaskSearchFilter;
import com.miotech.kun.workflow.common.task.service.TaskService;
import com.miotech.kun.workflow.common.task.vo.RunTaskVO;
import com.miotech.kun.workflow.common.task.vo.TaskPropsVO;
import com.miotech.kun.workflow.core.model.task.Task;
import com.miotech.kun.workflow.web.annotation.QueryParameter;
import com.miotech.kun.workflow.web.annotation.RequestBody;
import com.miotech.kun.workflow.web.annotation.RouteMapping;
import com.miotech.kun.workflow.web.annotation.RouteVariable;
import com.miotech.kun.workflow.web.entity.AcknowledgementVO;
import com.miotech.kun.workflow.common.task.vo.PaginationVO;

import java.util.List;
import java.util.Objects;

@Singleton
public class TaskController {

    private final TaskService taskService;

    @Inject
    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @RouteMapping(url= "/tasks", method = "GET")
    public PaginationVO<Task> getTask(@QueryParameter(defaultValue = "1") int pageNum,
                                @QueryParameter(defaultValue = "100") int pageSize,
                                @QueryParameter String name) {
        TaskSearchFilter filter = TaskSearchFilter.newBuilder()
                .withName(name)
                .withPageNum(pageNum)
                .withPageSize(pageSize)
                .build();
        return taskService.fetchTasksByFilters(filter);
    }

    @RouteMapping(url = "/tasks/_search", method = "POST")
    public PaginationVO<Task> searchTask(@RequestBody TaskSearchFilter taskSearchRequestBody) {
        // Assign default pagination values if not specified in request body
        TaskSearchFilter searchFilter = taskSearchRequestBody.cloneBuilder()
                .withPageNum(Objects.isNull(taskSearchRequestBody.getPageNum()) ? 1 : taskSearchRequestBody.getPageNum())
                .withPageSize(Objects.isNull(taskSearchRequestBody.getPageSize()) ? 100 : taskSearchRequestBody.getPageSize())
                .build();
        return taskService.fetchTasksByFilters(searchFilter);
    }

    @RouteMapping(url = "/tasks/{taskId}", method = "GET")
    public Task getTaskById(@RouteVariable Long taskId) {
        return taskService.find(taskId);
    }

    @RouteMapping(url= "/tasks", method = "POST")
    public Task createTask(@RequestBody TaskPropsVO taskBody) {
        return taskService.createTask(taskBody);
    }

    @RouteMapping(url= "/tasks/{taskId}", method = "DELETE")
    public AcknowledgementVO deleteTask(@RouteVariable Long taskId) {
        taskService.deleteTaskById(taskId);
        return new AcknowledgementVO("Delete success");
    }

    @RouteMapping(url= "/tasks/{taskId}", method = "PUT")
    public Task updateTask(@RouteVariable Long taskId, @RequestBody TaskPropsVO taskBody) {
        return taskService.fullUpdateTaskById(taskId, taskBody);
    }

    @RouteMapping(url= "/tasks/{taskId}", method = "PATCH")
    public Task partialUpdateTask(@RouteVariable Long taskId, @RequestBody TaskPropsVO taskBody) {
        return taskService.partialUpdateTask(taskId, taskBody);
    }

    @RouteMapping(url = "/tasks/{taskId}/neighbors", method = "GET")
    public Object getTaskNeighbors(@RouteVariable Long taskId,
                                      @QueryParameter(defaultValue = "1") int upstreamLevel,
                                      @QueryParameter(defaultValue = "1") int downstreamLevel
    ) {
        return taskService.getNeighbors(taskId, upstreamLevel, downstreamLevel);
    }

    @RouteMapping(url= "/tasks/_run", method = "POST")
    public Object runTasks(@RequestBody List<RunTaskVO> runTaskVOs) {
        return taskService.runTasks(runTaskVOs);
    }
}
