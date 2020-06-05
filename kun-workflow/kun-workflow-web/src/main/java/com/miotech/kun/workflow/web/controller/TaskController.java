package com.miotech.kun.workflow.web.controller;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.miotech.kun.workflow.common.exception.EntityNotFoundException;
import com.miotech.kun.workflow.common.task.filter.TaskSearchFilter;
import com.miotech.kun.workflow.common.task.service.TaskService;
import com.miotech.kun.workflow.common.task.vo.TaskPropsVO;
import com.miotech.kun.workflow.core.model.bo.RunTaskInfo;
import com.miotech.kun.workflow.core.model.task.Task;
import com.miotech.kun.workflow.web.annotation.QueryParameter;
import com.miotech.kun.workflow.web.annotation.RequestBody;
import com.miotech.kun.workflow.web.annotation.RouteMapping;
import com.miotech.kun.workflow.web.annotation.RouteVariable;
import com.miotech.kun.workflow.web.entity.AcknowledgementVO;

import java.util.List;

@Singleton
public class TaskController {

    private TaskService taskService;

    @Inject
    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @RouteMapping(url= "/tasks", method = "GET")
    public List<Task> getTask(@QueryParameter(defaultValue = "1") int pageNum,
                              @QueryParameter(defaultValue = "100") int pageSize,
                              @QueryParameter String name) {

        return taskService.fetchTasksByFilters(TaskSearchFilter.newBuilder()
                .withName(name)
                .withPageNum(pageNum)
                .withPageSize(pageSize)
                .build());
    }

    @RouteMapping(url = "/tasks/{taskId}", method = "GET")
    public Task getTaskById(@RouteVariable Long taskId) {
        return taskService.fetchTaskById(taskId).orElseThrow(() ->
                new EntityNotFoundException(String.format("Cannot find task with id: %s", taskId))
        );
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

    @RouteMapping(url= "/tasks/_run", method = "POST")
    public Object runTasks(@RequestBody List<RunTaskInfo> taskInfoList) {
        return taskService.runTasks(taskInfoList);
    }
}
