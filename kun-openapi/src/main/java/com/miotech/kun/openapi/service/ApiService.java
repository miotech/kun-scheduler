package com.miotech.kun.openapi.service;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
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
import com.miotech.kun.dataplatform.web.common.taskdefview.dao.TaskDefinitionViewDao;
import com.miotech.kun.dataplatform.web.common.taskdefview.service.TaskDefinitionViewService;
import com.miotech.kun.dataplatform.web.common.taskdefview.vo.CreateTaskDefViewRequest;
import com.miotech.kun.dataplatform.web.common.taskdefview.vo.TaskDefinitionViewSearchParams;
import com.miotech.kun.dataplatform.web.common.taskdefview.vo.TaskDefinitionViewVO;
import com.miotech.kun.dataplatform.web.common.tasktemplate.service.TaskTemplateService;
import com.miotech.kun.dataplatform.web.model.taskdefview.TaskDefinitionView;
import com.miotech.kun.openapi.model.*;
import com.miotech.kun.security.model.UserInfo;
import com.miotech.kun.security.service.BaseSecurityService;
import com.miotech.kun.workflow.client.WorkflowClient;
import com.miotech.kun.workflow.client.model.PaginationResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
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
    private TaskDefinitionViewDao taskDefinitionViewDao;

    @Autowired
    private TaskDefinitionService taskDefinitionService;

    @Autowired
    private DeployService deployService;

    @Autowired
    private WorkflowClient workflowClient;

    public String authenticate(UserRequest request) {
        UserInfo userInfo = getUserByUsername(request.getUsername());
        Preconditions.checkNotNull(userInfo, "User does not exist. Please check username");
        Preconditions.checkNotNull(userInfo.getId(), "User does not exist. Please check username");
        return JwtUtils.createToken(request.getUsername());
    }

    public TaskViewVO createTaskView(TaskViewCreateRequest request, String token) {
        setUserByToken(token);
        CreateTaskDefViewRequest createTaskDefViewRequest = CreateTaskDefViewRequest.builder()
                .name(request.getTaskViewName())
                .build();
        return TaskViewVO.from(taskDefinitionViewService.create(createTaskDefViewRequest));

    }

    public PageResult<TaskViewVO> getTaskViewList(Integer pageNum, Integer pageSize) {
        TaskDefinitionViewSearchParams searchParams = TaskDefinitionViewSearchParams.builder()
                .pageNum(pageNum)
                .pageSize(pageSize)
                .build();
        PageResult<TaskDefinitionViewVO> result = taskDefinitionViewService.searchPage(searchParams);
        return new PageResult<>(result.getPageSize(),
                result.getPageNumber(),
                result.getTotalCount(),
                result.getRecords().stream().map(TaskViewVO::from).collect(Collectors.toList()));
    }

    public TaskViewDetailVO getTaskViewDetail(Long taskViewId) {
        Optional<TaskDefinitionView> fetchedTaskView = taskDefinitionViewService.fetchById(taskViewId);
        Preconditions.checkArgument(fetchedTaskView.isPresent(), "Task view not found. Please re-check task view id.");
        TaskDefinitionView taskViewDetail = fetchedTaskView.get();
        return TaskViewDetailVO.from(taskViewDetail);
    }

    public PageResult<TaskViewVO> searchTaskView(String keyword) {
        List<TaskDefinitionView> result = taskDefinitionViewDao.fetchByTaskDefinitionViewName(keyword);
        return new PageResult<>(result.size(), 1, result.size(),
                result.stream().map(TaskViewVO::from).collect(Collectors.toList()));
    }

    public List<TaskTemplateVO> getTaskTemplateList() {
        return taskTemplateService.getAllTaskTemplates().stream()
                .map(TaskTemplateVO::from)
                .collect(Collectors.toList());
    }


    public PaginationResult<TaskVO> searchTask(TaskSearchRequest request) {
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
        PaginationResult<TaskVO> result = new PaginationResult<>(
                taskDefinitions.getPageSize(),
                taskDefinitions.getPageNum(),
                taskDefinitions.getTotalCount(),
                taskDefinitionService.convertToVOList(taskDefinitions.getRecords(), true)
                        .stream()
                        .map(this::convertToTaskVO)
                        .collect(Collectors.toList()));
        return result;
    }

    public TaskVO getTask(Long taskId) {
        return convertToTaskVO(taskDefinitionService.convertToVO(taskDefinitionService.find(taskId)));
    }

    public TaskVO createTask(TaskCreateRequest request, String token) {
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
        return convertToTaskVO(taskDefinitionService.convertToVO(taskDefinition));
    }


    public TaskVO updateTask(TaskUpdateRequest request, String token) {
        Long ownerId = setUserByToken(token);
        Preconditions.checkArgument((request.getTaskName() != null || request.getTaskPayload() != null),
                "At least update one of taskName or taskPayload");
        TaskDefinition originalTaskDef = taskDefinitionService.find(request.getTaskId());
        TaskDefinition taskDefinition = taskDefinitionService.update(request.getTaskId(),
                new UpdateTaskDefinitionRequest(request.getTaskId(),
                        request.getTaskName() == null? originalTaskDef.getName() : request.getTaskName(),
                        request.getTaskPayload() == null? originalTaskDef.getTaskPayload() : request.getTaskPayload(),
                        ownerId
                ));
        return convertToTaskVO(taskDefinitionService.convertToVO(taskDefinition));
    }

    public DeployVO deployTask(TaskCommitRequest request, String token) {
        setUserByToken(token);
        Deploy deploy = deployService.deployFast(request.getTaskId(),
                new CommitRequest(request.getTaskId(), request.getMessage()));
        return deployService.convertVO(deploy);
    }

    public boolean changeTaskRunPriority(TaskRunPriorityChangeRequest request, String token) {
        Long userId = setUserByToken(token);
        Integer priority = request.getPriority();
        Preconditions.checkArgument((priority > 0 && priority <= 5), "task run priority should be 1-5");
        return workflowClient.changeTaskRunPriority(request.getTaskRunId(), request.getPriority());
    }

    private Long setUserByToken(String token) {
        Preconditions.checkArgument(token.startsWith("Bearer "), "Token should in format: Bearer {token}");
        String username = JwtUtils.parseUsername(token);
        UserInfo userInfo = getUserByUsername(username);
        setCurrentUser(userInfo);
        return getCurrentUser().getId();
    }

    // TaskDefinitionVO is designed for web-frontend usage
    // convert TaskDefinitionVO to TaskVO for openapi usage with customization
    private TaskVO convertToTaskVO(TaskDefinitionVO taskDefinitionVO) {
        List<Long> taskViewIds = taskDefinitionViewService.fetchAllByTaskDefinitionId(taskDefinitionVO.getId())
                .stream()
                .map(TaskDefinitionViewVO::getId)
                .collect(Collectors.toList());
        return new TaskVO(taskDefinitionVO.getId(),
                taskDefinitionVO.getName(),
                taskDefinitionVO.getTaskTemplateName(),
                taskDefinitionVO.getTaskPayload(),
                taskDefinitionVO.getCreator(),
                taskDefinitionVO.isArchived(),
                taskDefinitionVO.isDeployed(),
                taskDefinitionVO.isUpdated(),
                taskDefinitionVO.getOwner(),
                taskDefinitionVO.getUpstreamTaskDefinitions(),
                taskDefinitionVO.getLastModifier(),
                taskDefinitionVO.getLastUpdateTime(),
                taskDefinitionVO.getCreateTime(),
                taskDefinitionVO.getTaskCommits(),
                taskViewIds);
    }
}
