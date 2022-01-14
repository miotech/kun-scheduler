package com.miotech.kun.openapi.controller;

import com.miotech.kun.common.model.PageRequest;
import com.miotech.kun.common.model.PageResult;
import com.miotech.kun.common.model.RequestResult;
import com.miotech.kun.commons.utils.JwtUtils;
import com.miotech.kun.dataplatform.web.common.deploy.vo.DeployVO;
import com.miotech.kun.dataplatform.web.common.taskdefinition.vo.TaskDefinitionVO;
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
        return RequestResult.success(JwtUtils.createToken(request.getUsername()));
    }

    @PostMapping("/task-view/create")
    public RequestResult<TaskViewVO> createTaskView(@RequestBody TaskViewCreateRequest request,
                                                    @RequestHeader("Authorization") String token) {
        return RequestResult.success(apiService.createTaskView(request, token));
    }

    @GetMapping("/task-view/list")
    public RequestResult<PageResult<TaskViewVO>> getTaskViewList(@RequestBody PageRequest request) {
        return RequestResult.success(apiService.getTaskViewList(request));
    }

    @GetMapping("/task-template/list")
    public RequestResult<List<TaskTemplateVO>> getTaskTemplateList() {
        return RequestResult.success(apiService.getTaskTemplateList());
    }

    @PostMapping("/task/search")
    public RequestResult<PaginationResult<TaskDefinitionVO>> searchTask(@RequestBody TaskSearchRequest request) {
        return RequestResult.success(apiService.searchTask(request));
    }

    @PostMapping("/task/create")
    public RequestResult<TaskDefinitionVO> createTask(@RequestBody TaskCreateRequest request,
                                                      @RequestHeader("Authorization") String token) {
        return RequestResult.success(apiService.createTask(request, token));
    }

    @PutMapping("/task/update")
    public RequestResult<TaskDefinitionVO> updateTask(@RequestBody TaskUpdateRequest request,
                                                      @RequestHeader("Authorization") String token) {
        return RequestResult.success(apiService.updateTask(request, token));
    }

    @PostMapping("/task/deploy")
    public RequestResult<DeployVO> deployTask(@RequestBody TaskCommitRequest request,
                                              @RequestHeader("Authorization") String token) {
        return RequestResult.success(apiService.deployTask(request, token));
    }
}
