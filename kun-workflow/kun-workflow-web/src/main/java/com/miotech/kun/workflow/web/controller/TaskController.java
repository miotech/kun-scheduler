package com.miotech.kun.workflow.web.controller;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.miotech.kun.workflow.common.task.service.TaskService;
import com.miotech.kun.workflow.web.annotation.RequestBody;
import com.miotech.kun.workflow.web.annotation.RouteMapping;
import com.miotech.kun.workflow.core.model.bo.TaskInfo;

import javax.servlet.http.HttpServletRequest;

@Singleton
public class TaskController {

    private TaskService taskService;

    @Inject
    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @RouteMapping(url= "/tasks", method = "GET")
    public Object getTask(HttpServletRequest request) {
        return null;
    }

    @RouteMapping(url= "/tasks", method = "POST")
    public Object createTask(@RequestBody TaskInfo taskBody) {
        return taskService.createTask(taskBody);
    }

    @RouteMapping(url= "/tasks", method = "DELETE")
    public Object deleteTask(HttpServletRequest request) {
        return null;
    }

    @RouteMapping(url= "/tasks", method = "PUT")
    public Object updateTask(HttpServletRequest request) {
        return null;
    }

}
