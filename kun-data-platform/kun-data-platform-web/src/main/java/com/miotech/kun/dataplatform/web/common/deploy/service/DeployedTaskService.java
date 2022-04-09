package com.miotech.kun.dataplatform.web.common.deploy.service;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.miotech.kun.commons.utils.DateTimeUtils;
import com.miotech.kun.dataplatform.facade.DeployedTaskFacade;
import com.miotech.kun.dataplatform.facade.model.commit.TaskCommit;
import com.miotech.kun.dataplatform.facade.model.commit.TaskSnapshot;
import com.miotech.kun.dataplatform.facade.model.deploy.DeployedTask;
import com.miotech.kun.dataplatform.facade.model.taskdefinition.*;
import com.miotech.kun.dataplatform.web.common.deploy.dao.DeployedTaskDao;
import com.miotech.kun.dataplatform.web.common.deploy.vo.*;
import com.miotech.kun.dataplatform.web.common.taskdefinition.dao.TaskRelationDao;
import com.miotech.kun.dataplatform.web.common.taskdefinition.service.TaskDefinitionService;
import com.miotech.kun.dataplatform.web.common.taskdefinition.vo.TaskRunLogVO;
import com.miotech.kun.dataplatform.web.common.tasktemplate.service.TaskTemplateService;
import com.miotech.kun.dataplatform.web.common.utils.DataPlatformIdGenerator;
import com.miotech.kun.dataplatform.web.common.utils.TagUtils;
import com.miotech.kun.monitor.facade.model.sla.SlaConfig;
import com.miotech.kun.monitor.facade.model.sla.TaskDefinitionNode;
import com.miotech.kun.monitor.facade.sla.SlaFacade;
import com.miotech.kun.security.model.UserInfo;
import com.miotech.kun.security.service.BaseSecurityService;
import com.miotech.kun.workflow.client.WorkflowClient;
import com.miotech.kun.workflow.client.model.*;
import com.miotech.kun.workflow.core.execution.Config;
import com.miotech.kun.workflow.core.model.common.Tag;
import com.miotech.kun.workflow.core.model.task.BlockType;
import com.miotech.kun.workflow.core.model.task.ScheduleConf;
import com.miotech.kun.workflow.core.model.task.ScheduleType;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunStatus;
import com.miotech.kun.workflow.core.model.taskrun.TimeType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.miotech.kun.dataplatform.web.common.utils.TagUtils.TAG_TASK_DEFINITION_ID_NAME;

@Service
@Slf4j
public class DeployedTaskService extends BaseSecurityService implements DeployedTaskFacade {

    private static final String TASK_RUN_ID_NOT_NULL = "`taskRunId` should not be null";

    private static final List<String> SCHEDULE_TYPE_FILTER = Lists.newArrayList("SCHEDULED");

    @Autowired
    private DeployedTaskDao deployedTaskDao;

    @Autowired
    private TaskRelationDao taskRelationDao;

    @Autowired
    private SlaFacade slaFacade;

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

    public Optional<DeployedTask> findOptional(Long taskDefinitionId) {
        Preconditions.checkNotNull(taskDefinitionId);
        return deployedTaskDao.fetchById(taskDefinitionId);
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
        TaskDefinition taskDefinition = taskDefinitionService.find(commit.getDefinitionId());
        DeployedTask task = find(commit.getDefinitionId());
        DeployedTask updated = task
                .cloneBuilder()
                .withName(remoteTask.getName())
                .withTaskCommit(commit)
                .withWorkflowTaskId(remoteTask.getId())
                .withOwner(taskDefinition.getOwner())
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
                scheduleConfig.getCronExpr(),
                scheduleConfig.getTimeZone(),
                BlockType.valueOf(scheduleConfig.getBlockType()));
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
        Preconditions.checkState(
                deployedTaskIds.size() == existedWorkflowIds.size(),
                "Failed on checking equality on {} deployed tasks corresponding to {} existing workflow task ids. Deployed task ids = {}; Workflow task ids = {}.",
                deployedTaskIds.size(),
                existedWorkflowIds.size(),
                deployedTaskIds,
                existedWorkflowIds
        );
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
                .withReties(scheduleConfig.getRetries())
                .withRetryDelay(scheduleConfig.getRetryDelay())
                .withExecutorLabel(scheduleConfig.getExecutorLabel())
                .build();
        List<Tag> searchTags = TagUtils.buildScheduleSearchTags(taskDefId, taskTemplateName);
        Task remoteTask = workflowClient.saveTask(task, searchTags);

        // update dependencies in neo4j
        updateDependenciesInNeo4j(deployedTaskIds, taskDefId, snapshot, remoteTask);
        return remoteTask;
    }

    private void updateDependenciesInNeo4j(List<Long> deployedTaskIds, Long taskDefId, TaskSnapshot snapshot, Task remoteTask) {
        SlaConfig slaConfig = snapshot.getTaskPayload().getScheduleConfig().getSlaConfig();
        Integer level = calculateLevel(slaConfig);
        Integer deadline = calculateDeadline(slaConfig, snapshot.getTaskPayload().getScheduleConfig().getTimeZone());
        slaFacade.updateDependencies(deployedTaskIds, taskDefId, snapshot.getName(), level, deadline, remoteTask.getId());
    }

    private Integer calculateDeadline(SlaConfig slaConfig, String timeZone) {
        if (slaConfig == null || timeZone == null) {
            return null;
        }

        return slaConfig.getDeadlineValue(timeZone);
    }

    private Integer calculateLevel(SlaConfig slaConfig) {
        if (slaConfig == null) {
            return null;
        }

        return slaConfig.getLevel();
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
        if ((request.getScheduleTypes() != null) && (!request.getScheduleTypes().isEmpty())) {
            searchRequestBuilder.withScheduleTypes(request.getScheduleTypes());
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

    public void removeTaskRunDependency(Long taskRunId, List<Long> upstreamTaskRunIds) {
        Preconditions.checkNotNull(taskRunId, TASK_RUN_ID_NOT_NULL);
        Preconditions.checkNotNull(upstreamTaskRunIds, "upstream task run id should not be null");
        Preconditions.checkArgument(!upstreamTaskRunIds.isEmpty(), "upstream task run id should not be empty");
        log.debug("taskrun is : {} and upstreamTaskRunIds are {} : ", taskRunId, upstreamTaskRunIds);
        workflowClient.removeTaskRunDependency(taskRunId, upstreamTaskRunIds);
    }

    public List<TaskRun> getUpstreamTaskRuns(Long taskRunId) {
        Preconditions.checkNotNull(taskRunId, TASK_RUN_ID_NOT_NULL);
        return getWorkFlowTaskRunDag(taskRunId, 1, 0).getNodes().stream()
                .filter(tr -> !Objects.equals(tr.getId(), taskRunId))
                .collect(Collectors.toList());
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

    public TaskRunGanttChart getTaskRunGantt(OffsetDateTime startTime, OffsetDateTime endTime,
                                             TimeType timeType, Long taskRunId) {
        if (startTime == null || endTime == null) {
            endTime = DateTimeUtils.now();
            startTime = endTime.minusDays(1);
        }
        if (timeType == null) {
            timeType = TimeType.startAt;
        }
        Preconditions.checkArgument(endTime.minusDays(7).isBefore(startTime), "time interval should be no more 7 days");
        if (taskRunId != null) {
            return workflowClient.getTaskRunGantt(taskRunId);
        } else {
            return workflowClient.getGlobalTaskRunGantt(startTime, endTime, timeType);
        }
    }

    public TaskRunLogVO getWorkFlowTaskRunLog(Long taskRunId) {
        return getWorkFlowTaskRunLog(taskRunId, -1);
    }

    public TaskRunLogVO getWorkFlowTaskRunLog(Long taskRunId, Integer attempt) {
        Preconditions.checkNotNull(taskRunId, TASK_RUN_ID_NOT_NULL);
        Preconditions.checkNotNull(attempt, "attempt cannot be null");
        TaskRunLog log = workflowClient.getLatestRunLog(taskRunId, attempt);
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

    public TaskRunLogVO getWorkFlowTaskRunLog(Long taskRunId, Integer start, Integer end) {
        return getWorkFlowTaskRunLog(taskRunId, start, end, -1);
    }

    public TaskRunLogVO getWorkFlowTaskRunLog(Long taskRunId, Integer start, Integer end, Integer attempt) {
        Preconditions.checkNotNull(taskRunId, TASK_RUN_ID_NOT_NULL);
        Preconditions.checkNotNull(attempt, "attempt cannot be null");
        TaskRunLog log = workflowClient.getLatestRunLog(taskRunId, start, end, attempt);
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

    public List<DeployedTaskWithRunVO> convertToListVOs(List<DeployedTask> deployedTasks, boolean scheduledOnly) {
        TaskRunSearchRequest searchRequest = TaskRunSearchRequest.newBuilder()
                .withTaskIds(deployedTasks.stream().map(DeployedTask::getWorkflowTaskId).collect(Collectors.toList()))
                .withPageNum(1)
                .withPageSize(deployedTasks.size() * 4)
                .withScheduleTypes(scheduledOnly ? SCHEDULE_TYPE_FILTER : null)
                .build();

        // Fetch corresponding task runs by batch querying workflow API
        PaginationResult<TaskRun> latestRuns = workflowClient.searchTaskRun(searchRequest);

        // Match taskRuns to deployed tasks one-by-one
        List<TaskRun> taskRuns = latestRuns.getRecords();
        List<DeployedTaskWithRunVO> results = new ArrayList<>();

        for (DeployedTask deployedTask : deployedTasks) {
            Optional<TaskRun> correspondingTaskRun = taskRuns.stream()
                    .filter(taskRun -> Objects.equals(taskRun.getTask().getId(), deployedTask.getWorkflowTaskId()))
                    .findAny();
            DeployedTaskWithRunVO vo = new DeployedTaskWithRunVO(
                    deployedTask.getDefinitionId(),
                    deployedTask.getWorkflowTaskId(),
                    deployedTask.getDefinitionId(),
                    deployedTask.getName(),
                    deployedTask.getTaskTemplateName(),
                    deployedTask.getOwner(),
                    deployedTask.isArchived(),
                    deployedTask.getTaskCommit().getSnapshot().getTaskPayload(),
                    correspondingTaskRun.orElse(null)
            );
            results.add(vo);
        }

        return results;
    }

    public UserInfo getUserByTaskId(Long wfTaskId) {
        Optional<DeployedTask> taskOptional = deployedTaskDao.fetchByWorkflowTaskId(wfTaskId);
        if(taskOptional.isPresent()){
            Long userId = taskDefinitionService.find(taskOptional.get().getDefinitionId()).getOwner();
            return getUserById(userId);
        }

        return null;
    }

    @Override
    public Map<Long, DeployedTask> findByWorkflowTaskIds(List<Long> taskIds) {
        if (CollectionUtils.isEmpty(taskIds)) {
            return Maps.newHashMap();
        }

        return deployedTaskDao.fetchByWorkflowTaskIds(taskIds);
    }

    public Optional<DeployedTask> findByWorkflowTaskId(Long workflowTaskId) {
        return deployedTaskDao.fetchByWorkflowTaskId(workflowTaskId);
    }

    public List<DeployedTask> fetchUnarchived() {
        return deployedTaskDao.fetchUnarchived();
    }

    public void rebuildRelationship() {
        List<DeployedTask> deployedTasks = fetchUnarchived();
        for (DeployedTask deployedTask : deployedTasks) {
            Long definitionId = deployedTask.getDefinitionId();
            TaskDefinition taskDefinition = taskDefinitionService.find(definitionId);
            ScheduleConfig scheduleConfig = taskDefinition.getTaskPayload().getScheduleConfig();
            // 写入图数据库
            SlaConfig slaConfig = scheduleConfig.getSlaConfig();
            Integer level = null;
            Integer deadline = null;
            if (slaConfig != null) {
                level = slaConfig.getLevel();
                deadline = slaConfig.getDeadlineValue(scheduleConfig.getTimeZone());
            }

            TaskDefinitionNode taskDefinitionNode = TaskDefinitionNode.from(definitionId, taskDefinition.getName(),
                    level, deadline, deployedTask.getWorkflowTaskId(), null);
            slaFacade.save(taskDefinitionNode);
        }

        List<TaskRelation> taskRelations = taskRelationDao.fetchAll();
        for (TaskRelation taskRelation : taskRelations) {
            // 建立关系
            slaFacade.bind(taskRelation.getUpstreamId(), taskRelation.getDownstreamId(), TaskDefinitionNode.Relationship.OUTPUT);
        }
    }

}
