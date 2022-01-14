package com.miotech.kun.openapi.service;

import com.google.common.base.Preconditions;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.miotech.kun.common.model.PageRequest;
import com.miotech.kun.common.model.PageResult;
import com.miotech.kun.commons.utils.JwtUtils;
import com.miotech.kun.dataplatform.facade.model.deploy.Deploy;
import com.miotech.kun.dataplatform.facade.model.taskdefinition.TaskDefinition;
import com.miotech.kun.dataplatform.web.common.commit.vo.CommitRequest;
import com.miotech.kun.dataplatform.web.common.deploy.service.DeployService;
import com.miotech.kun.dataplatform.web.common.deploy.vo.DeployVO;
import com.miotech.kun.dataplatform.web.common.taskdefinition.dao.TaskDefinitionDao;
import com.miotech.kun.dataplatform.web.common.taskdefinition.service.TaskDefinitionService;
import com.miotech.kun.dataplatform.web.common.taskdefinition.vo.CreateTaskDefinitionRequest;
import com.miotech.kun.dataplatform.web.common.taskdefinition.vo.TaskDefinitionSearchRequest;
import com.miotech.kun.dataplatform.web.common.taskdefinition.vo.TaskDefinitionVO;
import com.miotech.kun.dataplatform.web.common.taskdefinition.vo.UpdateTaskDefinitionRequest;
import com.miotech.kun.dataplatform.web.common.taskdefview.service.TaskDefinitionViewService;
import com.miotech.kun.dataplatform.web.common.taskdefview.vo.CreateTaskDefViewRequest;
import com.miotech.kun.dataplatform.web.common.taskdefview.vo.TaskDefinitionViewSearchParams;
import com.miotech.kun.dataplatform.web.common.taskdefview.vo.TaskDefinitionViewVO;
import com.miotech.kun.dataplatform.web.common.tasktemplate.service.TaskTemplateService;
import com.miotech.kun.openapi.model.*;
import com.miotech.kun.security.model.UserInfo;
import com.miotech.kun.security.service.BaseSecurityService;
import com.miotech.kun.workflow.client.model.PaginationResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ApiService extends BaseSecurityService {

    @Autowired
    private TaskDefinitionViewService taskDefinitionViewService;

    @Autowired
    private TaskTemplateService taskTemplateService;

    @Autowired
    private TaskDefinitionDao taskDefinitionDao;

    @Autowired
    private TaskDefinitionService taskDefinitionService;

    @Autowired
    private DeployService deployService;

    public TaskViewVO createTaskView(TaskViewCreateRequest request, String token) {
        setUserByToken(token);
        CreateTaskDefViewRequest createTaskDefViewRequest = CreateTaskDefViewRequest.builder()
                .name(request.getTaskViewName())
                .build();
        return TaskViewVO.from(taskDefinitionViewService.create(createTaskDefViewRequest));

    }

    public PageResult<TaskViewVO> getTaskViewList(PageRequest request) {
        TaskDefinitionViewSearchParams searchParams = TaskDefinitionViewSearchParams.builder()
                .pageNum(request.getPageNum())
                .pageSize(request.getPageSize())
                .build();
        PageResult<TaskDefinitionViewVO> result = taskDefinitionViewService.searchPage(searchParams);
        return new PageResult<>(result.getPageSize(),
                result.getPageNumber(),
                result.getTotalCount(),
                result.getRecords().stream().map(TaskViewVO::from).collect(Collectors.toList()));
    }

    public List<TaskTemplateVO> getTaskTemplateList() {
        return taskTemplateService.getAllTaskTemplates().stream()
                .map(TaskTemplateVO::from)
                .collect(Collectors.toList());
    }


    public PaginationResult<TaskDefinitionVO> searchTask(TaskSearchRequest request) {
        TaskDefinitionSearchRequest searchRequest = new TaskDefinitionSearchRequest(request.getPageSize(),
                request.getPageNum(),
                request.getTaskName(),
                request.getTaskTemplateName(),
                ImmutableList.of(),
                request.getOwnerId() == null? ImmutableList.of() : ImmutableList.of(request.getOwnerId()),
                Optional.of(false),
                request.getTaskViewId() == null? ImmutableList.of() : ImmutableList.of(request.getTaskViewId())
                );

        PaginationResult<TaskDefinition> taskDefinitions = taskDefinitionService.search(searchRequest);
        PaginationResult<TaskDefinitionVO> result = new PaginationResult<>(
                taskDefinitions.getPageSize(),
                taskDefinitions.getPageNum(),
                taskDefinitions.getTotalCount(),
                taskDefinitionService.convertToVOList(taskDefinitions.getRecords(), true)
        );
        return result;
    }


    public TaskDefinitionVO createTask(TaskCreateRequest request, String token) {
        Long ownerId = setUserByToken(token);
        TaskDefinition taskDefinition = taskDefinitionService.create(new CreateTaskDefinitionRequest(request.getTaskName(),
                request.getTaskTemplateName()));
        taskDefinitionViewService.putTaskDefinitionsIntoView(
                Collections.singleton(taskDefinition.getDefinitionId()),
                request.getTaskViewId());
        taskDefinition = taskDefinitionService.update(taskDefinition.getDefinitionId(),
                new UpdateTaskDefinitionRequest(taskDefinition.getDefinitionId(),
                        request.getTaskName(),
                        request.getTaskPayload(),
                        ownerId
                ));
        return taskDefinitionService.convertToVO(taskDefinition);
    }


    public TaskDefinitionVO updateTask(TaskUpdateRequest request, String token) {
        Long ownerId = setUserByToken(token);
        TaskDefinition taskDefinition = taskDefinitionService.update(request.getTaskId(),
                new UpdateTaskDefinitionRequest(request.getTaskId(),
                        request.getTaskName(),
                        request.getTaskPayload(),
                        ownerId
                ));
        return taskDefinitionService.convertToVO(taskDefinition);
    }

    public DeployVO deployTask(TaskCommitRequest request, String token) {
        setUserByToken(token);
        Deploy deploy = deployService.deployFast(request.getTaskId(),
                new CommitRequest(request.getTaskId(), request.getMessage()));
        return deployService.convertVO(deploy);
    }

    private Long setUserByToken(String token) {
        Preconditions.checkArgument(token.startsWith("Bearer "), "Token should in format: Bearer {token}");
        String username = JwtUtils.parseUsername(token);
        UserInfo userInfo = getUserByUsername(username);
        setCurrentUser(userInfo);
        return getCurrentUser().getId();
    }
}
