package com.miotech.kun.openapi.service;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.miotech.kun.common.model.PageResult;
import com.miotech.kun.commons.utils.JwtUtils;
import com.miotech.kun.dataplatform.facade.backfill.Backfill;
import com.miotech.kun.dataplatform.facade.model.deploy.Deploy;
import com.miotech.kun.dataplatform.facade.model.deploy.DeployedTask;
import com.miotech.kun.dataplatform.facade.model.taskdefinition.TaskDefinition;
import com.miotech.kun.dataplatform.web.common.backfill.service.BackfillService;
import com.miotech.kun.dataplatform.web.common.backfill.vo.BackfillDetailVO;
import com.miotech.kun.dataplatform.web.common.backfill.vo.BackfillSearchParams;
import com.miotech.kun.dataplatform.web.common.commit.vo.CommitRequest;
import com.miotech.kun.dataplatform.web.common.deploy.service.DeployService;
import com.miotech.kun.dataplatform.web.common.deploy.service.DeployedTaskService;
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
import com.miotech.kun.dataplatform.web.common.utils.TagUtils;
import com.miotech.kun.dataplatform.web.model.taskdefview.TaskDefinitionView;
import com.miotech.kun.metadata.core.model.datasource.DataSource;
import com.miotech.kun.openapi.model.response.DataSourceVO;
import com.miotech.kun.openapi.model.request.*;
import com.miotech.kun.openapi.model.response.*;
import com.miotech.kun.operationrecord.common.anno.OperationRecord;
import com.miotech.kun.operationrecord.common.model.OperationRecordType;
import com.miotech.kun.security.model.UserInfo;
import com.miotech.kun.security.service.BaseSecurityService;
import com.miotech.kun.workflow.client.WorkflowClient;
import com.miotech.kun.workflow.client.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

import static com.miotech.kun.dataplatform.web.common.utils.TagUtils.TAG_TASK_DEFINITION_ID_NAME;

@Service
public class ApiService extends BaseSecurityService {
    @Value("${metadata.base-url:localhost:8084}")
    String url;

    @Autowired
    private RestTemplate restTemplate;

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
    private DeployedTaskService deployedTaskService;

    @Autowired
    private WorkflowClient workflowClient;

    @Autowired
    private BackfillService backfillService;

    private static final List<String> SCHEDULE_TYPE_FILTER = Lists.newArrayList("SCHEDULED");


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

    public PageResult<TaskViewVO> getTaskViewList(TaskDefinitionViewSearchParams searchParams) {
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


    public PaginationResult<TaskVO> searchTask(TaskDefinitionSearchRequest searchRequest) {
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

    public TaskVOWithDependencies fetchTaskWithDependencies(Long taskDefinitionId, int upstreamLevel, int downstreamLevel) {
        DeployedTask deployedTask = deployedTaskService.find(taskDefinitionId);
        TaskWithDependencies taskWithDependencies = workflowClient.getTaskWithDependencies(deployedTask.getWorkflowTaskId(), upstreamLevel, downstreamLevel);

        Map<Long, Long> taskDefinitionIdMap = new HashMap<>();
        for (Task task : taskWithDependencies.getTasks()) {
            Long taskDefId = TagUtils.getTagValue(task.getTags(), TAG_TASK_DEFINITION_ID_NAME)
                    .map(Long::parseLong)
                    .get();
            taskDefinitionIdMap.put(task.getId(), taskDefId);
        }

        List<TaskVO> tasks = taskDefinitionService.convertToVOList(taskDefinitionService.findByDefIds(new ArrayList<>(taskDefinitionIdMap.values())), true)
                .stream()
                .map(this::convertToTaskVO).collect(Collectors.toList());
        List<TaskDependencyVO> dependencies = taskWithDependencies.getDependencies()
                .stream()
                .map(x -> new TaskDependencyVO(
                        taskDefinitionIdMap.get(x.getDownstreamTaskId()),
                        taskDefinitionIdMap.get(x.getUpstreamTaskId())))
                .collect(Collectors.toList());

        return new TaskVOWithDependencies(tasks, dependencies);
    }


    public TaskVO createTask(TaskCreateRequest request, String token) {
        String owner = setUserByToken(token);
        TaskDefinition taskDefinition = taskDefinitionService.create(new CreateTaskDefinitionRequest(request.getTaskName(),
                request.getTaskTemplateName()));
        taskDefinitionViewService.putTaskDefinitionsIntoView(
                Collections.singleton(taskDefinition.getDefinitionId()),
                request.getTaskViewId());
        taskDefinition = taskDefinitionService.update(taskDefinition.getDefinitionId(),
                new UpdateTaskDefinitionRequest(taskDefinition.getDefinitionId(),
                        request.getTaskName(),
                        request.getTaskPayload(),
                        owner
                ));
        return convertToTaskVO(taskDefinitionService.convertToVO(taskDefinition));
    }


    public TaskVO updateTask(TaskUpdateRequest request, String token) {
        setUserByToken(token);
        Preconditions.checkArgument((request.getTaskName() != null || request.getTaskPayload() != null || request.getOwner() != null),
                "At least update one of taskName or taskPayload or owner");
        if (request.getOwner() != null) {
            UserInfo userInfo = getUserByUsername(request.getOwner());
            Preconditions.checkNotNull(userInfo, "User does not exist. Please check username");
            Preconditions.checkNotNull(userInfo.getId(), "User does not exist. Please check username");
        }
        TaskDefinition originalTaskDef = taskDefinitionService.find(request.getTaskId());
        TaskDefinition taskDefinition = taskDefinitionService.update(request.getTaskId(),
                new UpdateTaskDefinitionRequest(request.getTaskId(),
                        request.getTaskName() == null ? originalTaskDef.getName() : request.getTaskName(),
                        request.getTaskPayload() == null ? originalTaskDef.getTaskPayload() : request.getTaskPayload(),
                        request.getOwner() == null ? originalTaskDef.getOwner() : request.getOwner()
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
        setUserByToken(token);
        Integer priority = request.getPriority();
        Preconditions.checkArgument((priority > 0 && priority <= 5), "task run priority should be 1-5");
        return workflowClient.changeTaskRunPriority(request.getTaskRunId(), request.getPriority());
    }


    public PaginationResult<TaskRun> fetchTaskRunList(TaskRunSearchRequest taskRunSearchRequest) {

        return workflowClient.searchTaskRun(taskRunSearchRequest);
    }

    public TaskRun fetchTaskRun(Long taskRunId) {
        return workflowClient.getTaskRun(taskRunId);
    }

    public PaginationResult<TaskRun> fetchTaskRunByTaskId(Long taskDefId, int pageSize, int pageNum) {
        TaskRunSearchRequest taskRunSearchRequest = TaskRunSearchRequest.newBuilder()
                .withScheduleTypes(SCHEDULE_TYPE_FILTER)
                .withTaskIds(Collections.singletonList(deployedTaskService.find(taskDefId).getWorkflowTaskId()))
                .withPageNum(pageNum)
                .withPageSize(pageSize)
                .build();
        return workflowClient.searchTaskRun(taskRunSearchRequest);
    }

    @OperationRecord(type = OperationRecordType.TASK_ABORT, args = {"#taskRunId"})
    public TaskRun abortTaskRun(Long taskRunId) {
        return workflowClient.stopTaskRun(taskRunId);
    }

    @OperationRecord(type = OperationRecordType.TASK_RERUN, args = {"#taskRunId"})
    public TaskRun restartTaskRun(Long taskRunId) {
        return workflowClient.restartTaskRun(taskRunId);
    }

    @OperationRecord(type = OperationRecordType.TASK_RERUN, args = {"#taskRunIds"})
    public void restartTaskRuns(List<Long> taskRunIds) {
        workflowClient.restartTaskRuns(taskRunIds);
    }

    @OperationRecord(type = OperationRecordType.TASK_SKIP, args = {"#taskRunId"})
    public TaskRun skipTaskRun(Long taskRunId) {
        return workflowClient.skipTaskRun(taskRunId);
    }

    @OperationRecord(type = OperationRecordType.TASK_REMOVE_DEPENDENCY, args = {"#taskRunId", "#upstreamTaskRunIds"})
    public void removeTaskRunDependency(Long taskRunId, List<Long> upstreamTaskRunIds) {
        workflowClient.removeTaskRunDependency(taskRunId, upstreamTaskRunIds);
    }

    public TaskRunWithDependencies fetchTaskRunWithDependencies(Long taskRunId, int upstreamLevel, int downstreamLevel) {
        return workflowClient.getTaskRunWithDependencies(taskRunId, upstreamLevel, downstreamLevel);
    }

    public PageResult<Backfill> fetchBackfillList(BackfillSearchParams backfillSearchParams) {
        return backfillService.search(backfillSearchParams);
    }

    public BackfillDetailVO fetchBackfillDetail(Long backfillId) {
        Optional<Backfill> backfillOptional = backfillService.fetchById(backfillId);
        List<TaskRun> taskRuns = backfillService.fetchTaskRunsByBackfillId(backfillId);
        return BackfillDetailVO.from(backfillOptional.get(), taskRuns);
    }

    public String setUserByToken(String token) {
        Preconditions.checkArgument(token.startsWith("Bearer "), "Token should in format: Bearer {token}");
        String username = JwtUtils.parseUsername(token);
        UserInfo userInfo = getUserByUsername(username);
        setCurrentUser(userInfo);
        return getCurrentUser().getUsername();
    }

    public DataSourceVO findDataSource(Long id) {
        return new DataSourceVO(findDataSourceById(id));
    }


    private DataSource findDataSourceById(Long id) {
        String createUrl = url + "/datasource/{id}";
        return restTemplate
                .exchange(createUrl, HttpMethod.GET, null, DataSource.class, id)
                .getBody();
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
