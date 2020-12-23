package com.miotech.kun.dataplatform.common.deploy.service;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.miotech.kun.dataplatform.common.deploy.dao.DeployedTaskDao;
import com.miotech.kun.dataplatform.common.deploy.vo.*;
import com.miotech.kun.dataplatform.common.taskdefinition.service.TaskDefinitionService;
import com.miotech.kun.dataplatform.common.taskdefinition.vo.TaskRunLogVO;
import com.miotech.kun.dataplatform.common.tasktemplate.service.TaskTemplateService;
import com.miotech.kun.dataplatform.common.utils.DataPlatformIdGenerator;
import com.miotech.kun.dataplatform.common.utils.TagUtils;
import com.miotech.kun.dataplatform.model.commit.TaskCommit;
import com.miotech.kun.dataplatform.model.commit.TaskSnapshot;
import com.miotech.kun.dataplatform.model.deploy.DeployedTask;
import com.miotech.kun.dataplatform.model.taskdefinition.ScheduleConfig;
import com.miotech.kun.dataplatform.model.taskdefinition.TaskConfig;
import com.miotech.kun.dataplatform.model.taskdefinition.TaskDatasetProps;
import com.miotech.kun.dataplatform.model.taskdefinition.TaskPayload;
import com.miotech.kun.dataplatform.model.taskdefinition.*;
import com.miotech.kun.security.model.UserInfo;
import com.miotech.kun.workflow.client.WorkflowClient;
import com.miotech.kun.workflow.client.model.*;
import com.miotech.kun.workflow.core.execution.Config;
import com.miotech.kun.workflow.core.model.common.Tag;
import com.miotech.kun.workflow.core.model.task.ScheduleConf;
import com.miotech.kun.workflow.core.model.task.ScheduleType;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import com.miotech.kun.security.service.BaseSecurityService;

import java.util.*;
import java.util.stream.Collectors;

import static com.miotech.kun.dataplatform.common.utils.TagUtils.TAG_TASK_DEFINITION_ID_NAME;

@Service
@Slf4j
public class DeployedTaskService extends BaseSecurityService{

    private static final String TASK_RUN_ID_NOT_NULL = "`taskRunId` should not be null";

    @Autowired
    private DeployedTaskDao deployedTaskDao;

    @Autowired
    @Lazy
    private WorkflowClient workflowClient;

    @Autowired
    private TaskTemplateService taskTemplateService;

    @Autowired
    private TaskDefinitionService taskDefinitionService;

    public DeployedTask find(Long definitionId) {
        return deployedTaskDao.fetchById(definitionId)
                .<IllegalArgumentException>orElseThrow(() -> {
                    throw new IllegalArgumentException(String.format("Deployed Task not found: \"%s\"", definitionId));
                });
    }

    public List<DeployedTask> findByDefIds(List<Long> defIds){
        return deployedTaskDao.fetchByIds(defIds);
    }

    public PaginationResult<DeployedTask> search(DeployedTaskSearchRequest request) {
        Preconditions.checkArgument(request.getPageNum() > 0, "page number should be a positive number");
        Preconditions.checkArgument(request.getPageSize() > 0, "page size should be a positive number");

        return deployedTaskDao.search(request);
    }

    public Task getWorkFlowTask(Long definitionId) {
        DeployedTask deployedTask = find(definitionId);
        return workflowClient.getTask(deployedTask.getWorkflowTaskId());
    }

    public DeployedTask deployTask(TaskCommit commit) {
        log.debug("\"{}\" task using commit \"{}\"", commit.getCommitType(), commit.getId());
        Optional<DeployedTask> deployedTaskOptional = deployedTaskDao.fetchById(commit.getDefinitionId());
        switch (commit.getCommitType()) {
            case CREATED:
            case MODIFIED:
                if (deployedTaskOptional.isPresent()) {
                    return updateDeployedTask(commit);
                }
                return createDeployedTask(commit);
            case OFFLINE:
                if (!deployedTaskOptional.isPresent()) {
                    throw new IllegalArgumentException("Could'nt offline deployed task, deployed task not found: " + commit.getDefinitionId());
                }
                return offlineDeployedTask(commit);
            default:
                throw new IllegalStateException("Unexpected value: " + commit.getCommitType());
        }
    }

    private DeployedTask createDeployedTask(TaskCommit commit) {
        TaskSnapshot snapshot = commit.getSnapshot();
        String taskTemplateName = snapshot.getTaskTemplateName();

        Task remoteTask = buildTaskFromCommit(commit);
        DeployedTask deployedTask = DeployedTask.newBuilder()
                .withId(DataPlatformIdGenerator.nextDeployedTaskId())
                .withDefinitionId(commit.getDefinitionId())
                .withName(remoteTask.getName())
                .withTaskCommit(commit)
                .withWorkflowTaskId(remoteTask.getId())
                .withTaskTemplateName(taskTemplateName)
                .withOwner(snapshot.getOwner())
                .withArchived(false)
                .build();
        deployedTaskDao.create(deployedTask);
        return deployedTask;
    }

    private DeployedTask updateDeployedTask(TaskCommit commit) {
        Task remoteTask = buildTaskFromCommit(commit);
        DeployedTask task = find(commit.getDefinitionId());
        DeployedTask updated = task
                .cloneBuilder()
                .withName(remoteTask.getName())
                .withTaskCommit(commit)
                .withWorkflowTaskId(remoteTask.getId())
                .build();
        deployedTaskDao.update(updated);
        return updated;
    }

    private DeployedTask offlineDeployedTask(TaskCommit commit) {
        DeployedTask task = find(commit.getDefinitionId());
        Task remoteTask = workflowClient.getTask(task.getWorkflowTaskId());
        // remote upstream dependencies and do not schedule anymore
        List<Tag> updatedTags = TagUtils.buildScheduleRunTags(
                task.getDefinitionId(),
                commit.getId(),
                task.getTaskTemplateName(),
                task.getOwner(),
                true);
        workflowClient.saveTask(remoteTask.cloneBuilder()
                .withDependencies(new ArrayList<>())
                .withScheduleConf(new ScheduleConf(ScheduleType.NONE, ""))
                .withTags(updatedTags)
                .build(), null);
        DeployedTask updated = task.cloneBuilder()
                .withTaskCommit(commit)
                .withArchived(true)
                .build();
        deployedTaskDao.update(updated);

        return updated;
    }

    private Task buildTaskFromCommit(TaskCommit commit) {
        TaskSnapshot snapshot = commit.getSnapshot();
        String taskTemplateName = snapshot.getTaskTemplateName();
        Long operatorId = taskTemplateService.find(taskTemplateName)
                .getOperator()
                .getId();

        // build task parameter
        Long taskDefId = commit.getDefinitionId();
        TaskPayload taskPayload = snapshot.getTaskPayload();
        // prepare schedule config
        ScheduleConfig scheduleConfig = taskPayload.getScheduleConfig();
        ScheduleConf scheduleConf = new ScheduleConf(
                ScheduleType.valueOf(scheduleConfig.getType()),
                scheduleConfig.getCronExpr());
        // prepare dependencies
        List<Long> deployedTaskIds = new ArrayList<>();
        List<Long> inputNodes = taskPayload.getScheduleConfig().getInputNodes();
        deployedTaskIds.addAll( inputNodes == null ? ImmutableList.of(): inputNodes);
        List<Long> inputDatasets = Optional.ofNullable(taskPayload.getScheduleConfig()
                .getInputDatasets())
                .map( x -> x.stream()
                        .map(TaskDatasetProps::getDefinitionId)
                        .collect(Collectors.toList()))
                .orElseGet(ImmutableList::of);
        deployedTaskIds.addAll(inputDatasets);
        List<Long> existedWorkflowIds = deployedTaskDao.fetchWorkflowTaskId(deployedTaskIds);
        Preconditions.checkArgument(deployedTaskIds.size() == existedWorkflowIds.size());
        List<TaskDependency> dependencies = existedWorkflowIds.stream()
                .map(x -> new TaskDependency(x, "latestTaskRun"))
                .distinct()
                .collect(Collectors.toList());
        // prepare task
        TaskDefinition taskDefinition = TaskDefinition.newBuilder()
                .withName(snapshot.getName())
                .withTaskTemplateName(snapshot.getTaskTemplateName())
                .withTaskPayload(snapshot.getTaskPayload())
                .withOwner(snapshot.getOwner())
                .build();
        TaskConfig taskConfig = taskTemplateService.getTaskConfig(taskPayload.getTaskConfig(), taskTemplateName, taskDefinition);
        Task task = Task.newBuilder()
                .withName(snapshot.getName())
                .withOperatorId(operatorId)
                .withDescription("Deployed Data Platform Task : " + taskDefId)
                .withConfig(new Config(taskConfig.getParams()))
                .withTags(TagUtils.buildScheduleRunTags(taskDefId, commit.getId(), taskTemplateName, snapshot.getOwner()))
                .withDependencies(dependencies)
                .withScheduleConf(scheduleConf)
                .build();
        List<Tag> searchTags = TagUtils.buildScheduleSearchTags(taskDefId, taskTemplateName);
        return workflowClient.saveTask(task, searchTags);
    }

    public DeployedTaskDAG getDeployedTaskDag(Long definitionId, int upstreamLevel, int downstreamLevel) {
        DeployedTask deployedTask = find(definitionId);
        Preconditions.checkArgument(upstreamLevel >=0 , "upstreamLevel should be non negative");
        Preconditions.checkArgument(downstreamLevel >=0 , "downstreamLevel should be non negative");

        TaskDAG taskDAG = workflowClient.getTaskDAG(deployedTask.getWorkflowTaskId(), upstreamLevel, downstreamLevel);
        // convert to deployed tasks
        Map<Long, Long> taskDefinitionIdMap = new HashMap<>();
        for (Task task : taskDAG.getNodes()) {
            Long taskDefinitionId = TagUtils.getTagValue(task.getTags(), TAG_TASK_DEFINITION_ID_NAME)
                    .map(Long::parseLong)
                    .get();
            taskDefinitionIdMap.put(task.getId(), taskDefinitionId);
        }

        List<DeployedTaskVO> tasks = deployedTaskDao.fetchByIds(new ArrayList<>(taskDefinitionIdMap.values()))
                .stream().map(this::convertVO).collect(Collectors.toList());
        List<DeployedTaskDependencyVO> dependencies = taskDAG.getEdges()
                .stream()
                .map(x -> new DeployedTaskDependencyVO(
                        taskDefinitionIdMap.get(x.getDownstreamTaskId()),
                        taskDefinitionIdMap.get(x.getUpstreamTaskId())))
                .collect(Collectors.toList());
        return new DeployedTaskDAG(
                tasks,
                dependencies
        );
    }

    public List<Long> getDownStreamWorkflowTasks(Long definitionId){
        try{
            DeployedTask deployedTask = find(definitionId);

            TaskDAG taskDAG = workflowClient.getTaskDAG(deployedTask.getWorkflowTaskId(), 1, 1);

            List<Long> downStreamTaskIds = taskDAG.getEdges()
                    .stream()
                    .filter(x -> x.getUpstreamTaskId() == deployedTask.getWorkflowTaskId())
                    .map(x -> x.getDownstreamTaskId())
                    .collect(Collectors.toList());
            return downStreamTaskIds;
        }catch (IllegalArgumentException e){
            return new ArrayList<Long>();
        }
    }

    public Task removeTaskDependendy(Long taskId, Set<Long> dependentTaskIds){
        Task task = workflowClient.getTask(taskId);
        Set<Long> dependencies = task.getDependencies().stream().map(x -> x.getUpstreamTaskId()).collect(Collectors.toSet());
        dependencies.removeAll(dependentTaskIds);

        List<TaskDependency> depen = dependencies.stream()
                .map(x -> new TaskDependency(x, "latestTaskRun"))
                .distinct()
                .collect(Collectors.toList());

        return task.cloneBuilder().withDependencies(depen).build();
    }

    /*------ Deployed Task Runs -----*/
    public PaginationResult<TaskRun> searchTaskRun(ScheduledTaskRunSearchRequest request) {
        Preconditions.checkArgument(request.getPageNum() > 0, "page number should be a positive number");
        Preconditions.checkArgument(request.getPageSize() > 0, "page size should be a positive number");
        TaskRunSearchRequest.Builder searchRequestBuilder = TaskRunSearchRequest.newBuilder()
                .withPageNum(request.getPageNum())
                .withPageSize(request.getPageSize())
                .withDateFrom(request.getStartTime())
                .withDateTo(request.getEndTime());
        // fetch workflow ids using deployed tasks
        if (!request.getDefinitionIds().isEmpty()) {
            List<Long> taskIds = deployedTaskDao.fetchWorkflowTaskId(request.getDefinitionIds());
            searchRequestBuilder.withTaskIds(taskIds);
        }
        if (org.apache.commons.lang3.StringUtils.isNoneBlank(request.getName())) {
            searchRequestBuilder.withName(request.getName());
        }
        if (request.getStatus() != null) {
            searchRequestBuilder.withStatus(Sets.newHashSet(request.getStatus()));
        }

        List<Tag> filterTags = TagUtils.buildScheduleSearchTags();
        // filter TaskTemplateName by tag, if using workflow ids might match many
        if (org.apache.commons.lang3.StringUtils.isNoneBlank(request.getTaskTemplateName())) {
            filterTags.add(new Tag(TagUtils.TAG_TASK_TEMPLATE_NAME, request.getTaskTemplateName()));
        }
        if (request.getOwnerId().isPresent()) {
            filterTags.add(new Tag(TagUtils.TAG_OWNER_NAME, request.getOwnerId().get().toString()));
        }

        TaskRunSearchRequest searchRequest = searchRequestBuilder
                .withTags(filterTags)
                .build();
        return workflowClient.searchTaskRun(searchRequest);
    }

    public TaskRun getWorkFlowTaskRun(Long taskRunId) {
        Preconditions.checkNotNull(taskRunId, TASK_RUN_ID_NOT_NULL);
        return workflowClient.getTaskRun(taskRunId);
    }

    public TaskRunDAG getWorkFlowTaskRunDag(Long taskRunId, int upstreamLevel, int downstreamLevel) {
        Preconditions.checkNotNull(taskRunId, TASK_RUN_ID_NOT_NULL);
        Preconditions.checkArgument(upstreamLevel >=0 , "upstreamLevel should be non negative");
        Preconditions.checkArgument(downstreamLevel >=0 , "downstreamLevel should be non negative");
        return workflowClient.getTaskRunDAG(taskRunId, upstreamLevel, downstreamLevel);
    }

    public TaskRunLogVO getWorkFlowTaskRunLog(Long taskRunId) {
        Preconditions.checkNotNull(taskRunId, TASK_RUN_ID_NOT_NULL);
        TaskRunLog log = workflowClient.getLatestRunLog(taskRunId);
        TaskRunStatus status = workflowClient.getTaskRunState(taskRunId)
                .getStatus();
        return new TaskRunLogVO(
                log.getTaskRunId(),
                log.getAttempt(),
                log.getStartLine(),
                log.getEndLine(),
                log.getLogs(),
                status.isFinished(),
                status
        );
    }

    public DeployedTaskVO convertVO(DeployedTask deployedTask) {
        return new DeployedTaskVO(
                deployedTask.getDefinitionId(),
                deployedTask.getWorkflowTaskId(),
                deployedTask.getDefinitionId(),
                deployedTask.getName(),
                deployedTask.getTaskTemplateName(),
                deployedTask.getOwner(),
                deployedTask.isArchived(),
                deployedTask.getTaskCommit().getSnapshot().getTaskPayload()
        );
    }

    public DeployedTaskWithRunVO convertToListVO(DeployedTask deployedTask) {
        TaskRunSearchRequest searchRequest = TaskRunSearchRequest.newBuilder()
                .withTaskIds(Collections.singletonList(deployedTask.getWorkflowTaskId()))
                .withPageNum(1)
                .withPageSize(1)
                .build();

        PaginationResult<TaskRun> latestRuns = workflowClient.searchTaskRun(searchRequest);
        TaskRun taskRun = latestRuns.getTotalCount() > 0 ? latestRuns.getRecords().get(0) : null;

        return new DeployedTaskWithRunVO(
                deployedTask.getDefinitionId(),
                deployedTask.getWorkflowTaskId(),
                deployedTask.getDefinitionId(),
                deployedTask.getName(),
                deployedTask.getTaskTemplateName(),
                deployedTask.getOwner(),
                deployedTask.isArchived(),
                deployedTask.getTaskCommit().getSnapshot().getTaskPayload(),
                taskRun
        );
    }

    public List<String> getUserByTaskId(Long wfTaskId){
        List<String> userList = new ArrayList<>();
        Optional<DeployedTask> taskOptional = deployedTaskDao.fetchByWorkflowTaskId(wfTaskId);
        if(taskOptional.isPresent()){
            Long userId = taskOptional.get().getOwner();
            UserInfo userInfo = getUserById(userId);
            if(userId != null)
                userList.add(userInfo.getUsername());
        }
        return userList;
    }
}
