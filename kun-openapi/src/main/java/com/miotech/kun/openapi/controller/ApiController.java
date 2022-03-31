package com.miotech.kun.openapi.controller;

import com.miotech.kun.common.model.PageResult;
import com.miotech.kun.common.model.RequestResult;
import com.miotech.kun.dataplatform.web.common.deploy.vo.DeployVO;
import com.miotech.kun.openapi.model.*;
import com.miotech.kun.openapi.service.ApiService;
import com.miotech.kun.workflow.client.model.PaginationResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@RestController
@RequestMapping("/kun/open-api")
public class ApiController {

    @Autowired
    private ApiService apiService;

    @PostMapping("/auth")
    public RequestResult<Object> authenticate(@RequestBody UserRequest request) {
        return RequestResult.success(apiService.authenticate(request));
    }

    @PostMapping("/task-view/create")
    public RequestResult<TaskViewVO> createTaskView(@RequestBody TaskViewCreateRequest request,
                                                    @RequestHeader("Authorization") String token) {
        return RequestResult.success(apiService.createTaskView(request, token));
    }

    @GetMapping("/task-view/list")
    public RequestResult<PageResult<TaskViewVO>> getTaskViewList(@RequestParam Integer pageSize,
                                                                 @RequestParam Integer pageNum) {
        return RequestResult.success(apiService.getTaskViewList(pageNum, pageSize));
    }

    @GetMapping("/task-view/search")
    public RequestResult<PageResult<TaskViewVO>> searchTaskView(@RequestParam String keyword) {
        return RequestResult.success(apiService.searchTaskView(keyword));
    }

    @GetMapping("/task-view/get")
    public RequestResult<TaskViewDetailVO> getTaskViewDetail(@RequestParam Long taskViewId) {
        return RequestResult.success(apiService.getTaskViewDetail(taskViewId));
    }

    @GetMapping("/task-template/list")
    public RequestResult<List<TaskTemplateVO>> getTaskTemplateList() {
        return RequestResult.success(apiService.getTaskTemplateList());
    }

    @GetMapping("/task/get")
    public RequestResult<TaskVO> getTask(@RequestParam Long taskId) {
        return RequestResult.success(apiService.getTask(taskId));
    }
    
    @PostMapping("/task/search")
    public RequestResult<PaginationResult<TaskVO>> searchTask(@RequestBody TaskSearchRequest request) {
        return RequestResult.success(apiService.searchTask(request));
    }

    @PostMapping("/task/create")
    public RequestResult<TaskVO> createTask(@RequestBody TaskCreateRequest request,
                                                      @RequestHeader("Authorization") String token) {
        return RequestResult.success(apiService.createTask(request, token));
    }

    @PutMapping("/task/update")
    public RequestResult<TaskVO> updateTask(@RequestBody TaskUpdateRequest request,
                                                      @RequestHeader("Authorization") String token) {
        return RequestResult.success(apiService.updateTask(request, token));
    }

    @PostMapping("/task/deploy")
    public RequestResult<DeployVO> deployTask(@RequestBody TaskCommitRequest request,
                                              @RequestHeader("Authorization") String token) {
        return RequestResult.success(apiService.deployTask(request, token));
    }

    @PostMapping("/taskrun/changePriority")
    public RequestResult<Object> changeTaskRunPriority(@RequestBody TaskRunPriorityChangeRequest request,
                                                       @RequestHeader("Authorization") String token) {
        return RequestResult.success(apiService.changeTaskRunPriority(request, token));
    }
}
