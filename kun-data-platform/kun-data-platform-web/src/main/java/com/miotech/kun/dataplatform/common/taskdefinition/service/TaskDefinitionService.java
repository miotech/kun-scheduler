package com.miotech.kun.dataplatform.common.taskdefinition.service;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.miotech.kun.dataplatform.common.commit.service.TaskCommitService;
import com.miotech.kun.dataplatform.common.datastore.service.DatasetService;
import com.miotech.kun.dataplatform.common.deploy.service.DeployService;
import com.miotech.kun.dataplatform.common.deploy.service.DeployedTaskService;
import com.miotech.kun.dataplatform.common.deploy.vo.DeployRequest;
import com.miotech.kun.dataplatform.common.taskdefinition.dao.TaskDefinitionDao;
import com.miotech.kun.dataplatform.common.taskdefinition.dao.TaskTryDao;
import com.miotech.kun.dataplatform.common.taskdefinition.vo.*;
import com.miotech.kun.dataplatform.common.tasktemplate.service.TaskTemplateService;
import com.miotech.kun.dataplatform.common.utils.DataPlatformIdGenerator;
import com.miotech.kun.dataplatform.common.utils.TagUtils;
import com.miotech.kun.dataplatform.model.commit.TaskCommit;
import com.miotech.kun.dataplatform.model.deploy.Deploy;
import com.miotech.kun.dataplatform.model.deploy.DeployedTask;
import com.miotech.kun.dataplatform.model.taskdefinition.*;
import com.miotech.kun.dataplatform.model.tasktemplate.ParameterDefinition;
import com.miotech.kun.dataplatform.model.tasktemplate.TaskTemplate;
import com.miotech.kun.security.service.BaseSecurityService;
import com.miotech.kun.workflow.client.WorkflowClient;
import com.miotech.kun.workflow.client.model.*;
import com.miotech.kun.workflow.core.execution.Config;
import com.miotech.kun.workflow.core.model.task.ScheduleConf;
import com.miotech.kun.workflow.core.model.task.ScheduleType;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunStatus;
import com.miotech.kun.workflow.utils.DateTimeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TaskDefinitionService extends BaseSecurityService {

    @Autowired
    private TaskDefinitionDao taskDefinitionDao;

    @Autowired
    private TaskTryDao taskTryDao;

    @Autowired
    private TaskTemplateService taskTemplateService;

    @Autowired
    private TaskCommitService taskCommitService;

    @Autowired
    private DatasetService datasetService;

    @Autowired
    private DeployedTaskService deployedTaskService;

    @Autowired
    private DeployService deployService;

    @Autowired
    @Lazy
    private WorkflowClient workflowClient;

    public TaskDefinition find(Long taskDefId) {
        return taskDefinitionDao.fetchById(taskDefId)
                .<IllegalArgumentException>orElseThrow(() -> {
                    throw new IllegalArgumentException(String.format("Task definition not found: \"%s\"", taskDefId));
                });
    }

    /**
     *
     * @return
     */
    public List<TaskDefinition> findAll() {
        return taskDefinitionDao.fetchAll();
    }

    public PaginationResult<TaskDefinition> search(TaskDefinitionSearchRequest searchRequest) {
        return taskDefinitionDao.search(searchRequest);
    }

    public TaskDefinition create(CreateTaskDefinitionRequest props) {
        Preconditions.checkNotNull(props);
        String taskName = props.getName();
        Preconditions.checkNotNull(taskName, "task definition name should be null");

        String taskTemplateName = props.getTaskTemplateName();
        Preconditions.checkNotNull(taskTemplateName, "task template name should be null");
        Preconditions.checkNotNull(taskTemplateService.find(taskTemplateName), "task template name not valid");

        Long currentUser = getCurrentUser().getId();
        Preconditions.checkNotNull(currentUser, "Current user id should not be `null`");
        TaskPayload taskPayload = TaskPayload.newBuilder()
                .withScheduleConfig(ScheduleConfig.newBuilder()
                        .withInputNodes(ImmutableList.of())
                        .withInputDatasets(ImmutableList.of())
                        .withOutputDatasets(ImmutableList.of())
                        .build())
                .withTaskConfig(new HashMap<>())
                .build();
        TaskDefinition taskDefinition = TaskDefinition.newBuilder()
                .withId(DataPlatformIdGenerator.nextTaskDefinitionId())
                .withDefinitionId(DataPlatformIdGenerator.nextDefinitionId())
                .withName(taskName)
                .withTaskTemplateName(taskTemplateName)
                .withTaskPayload(taskPayload)
                .withArchived(false)
                .withOwner(currentUser)
                .withCreator(currentUser)
                .withLastModifier(currentUser)
                .withCreateTime(DateTimeUtils.now())
                .withUpdateTime(DateTimeUtils.now())
                .build();

        taskDefinitionDao.create(taskDefinition);
        return taskDefinition;
    }

    /**
     * only allow update name and taskPayload and owner
     * leave check params and scheduleConfig before commit and deploy
     * @param definitionId
     * @param request
     * @return
     */
    @Transactional
    public TaskDefinition update(Long definitionId,
                                 UpdateTaskDefinitionRequest request) {
        Preconditions.checkArgument(StringUtils.isNoneBlank(request.getName()), "name should not be empty");
        Preconditions.checkNotNull(request.getOwner(), "owner should not be `null`");
        Preconditions.checkNotNull(request.getTaskPayload(), "taskPayload should not be `null`");

        TaskDefinition taskDefinition = find(definitionId);
        TaskDefinition updated = taskDefinition.cloneBuilder()
                .withTaskPayload(request.getTaskPayload())
                .withName(request.getName())
                .withOwner(request.getOwner())
                .withLastModifier(getCurrentUser().getId())
                .withUpdateTime(DateTimeUtils.now())
                .build();
        taskDefinitionDao.update(updated);

        // update output datasets
        // TODO: validate datasets
        List<TaskDatasetProps> outputDatasets = updated.getTaskPayload()
                .getScheduleConfig().getOutputDatasets();
        datasetService.createTaskDatasets(definitionId, outputDatasets);
        return updated;
    }

    public void checkRule(TaskDefinition taskDefinition) {
        Preconditions.checkNotNull(taskDefinition.getTaskPayload(), "taskPayload should not be `null`");
        Preconditions.checkArgument(taskDefinition.getOwner() > 0, "owner should specified");
        TaskTemplate taskTemplate = taskTemplateService.find(taskDefinition.getTaskTemplateName());
        this.checkParams(taskTemplate, taskDefinition.getTaskPayload().getTaskConfig());
        this.checkSchedule(taskDefinition);
    }

    /**
     * check all  required params has been filled in
     */
    private void checkParams(TaskTemplate taskTemplate, Map<String, Object> taskConfig) {
        Preconditions.checkNotNull(taskConfig, "taskConfig should not be `null`");
        List<String> requiredParameterNames = taskTemplate.getDisplayParameters()
                .stream()
                .filter(ParameterDefinition::isRequired)
                .map(ParameterDefinition::getName)
                .collect(Collectors.toList());
        if (!requiredParameterNames.isEmpty()) {
            for (String key: requiredParameterNames) {
                Object value = taskConfig.get(key);
                if (Objects.isNull(value)) {
                    throw new IllegalArgumentException(
                            String.format("Parameter \"%s\" should not be null or empty", key)
                    );
                }
            }
        }
    }

    private void checkSchedule(TaskDefinition taskDefinition) {
        Preconditions.checkNotNull(taskDefinition.getTaskPayload(), "taskPayload should not be `null`");
        Preconditions.checkNotNull(taskDefinition.getTaskPayload().getScheduleConfig(), "scheduleConfig should not be `null`");

        ScheduleConfig scheduleConfig = taskDefinition.getTaskPayload()
                .getScheduleConfig();

        // check schedule is valid
        List<ScheduleType> validScheduleType = new ArrayList<>();
        validScheduleType.add(ScheduleType.SCHEDULED);
        validScheduleType.add(ScheduleType.ONESHOT);
        Preconditions.checkNotNull(scheduleConfig.getType(), "Schedule type should not be null");
        ScheduleType scheduleType = ScheduleType.valueOf(scheduleConfig.getType());
        Preconditions.checkArgument(validScheduleType.contains(scheduleType),
                "ScheduleType should not be in " + validScheduleType.stream()
                        .map(ScheduleType::name)
                        .collect(Collectors.joining(",")));
        Preconditions.checkArgument(StringUtils.isNoneBlank(scheduleConfig.getCronExpr()),
                "Cron Expression should not be blank");
        // check dependency is valid
        List<Long> dependencyNodes = resolveUpstreamTaskDefIds(taskDefinition.getTaskPayload());
        List<Long> taskDefinitionIds = taskDefinitionDao.fetchByIds(dependencyNodes)
                .stream().map(TaskDefinition::getDefinitionId).collect(Collectors.toList());
        Collection<String> nonExisted = CollectionUtils.subtract(dependencyNodes, taskDefinitionIds)
                .stream().map(Object::toString).collect(Collectors.toList());
        Preconditions.checkArgument(nonExisted.isEmpty(), "Task DefinitionId not existed " + String.join(",", nonExisted));
    }

    public void delete(Long taskDefId) {
        Optional<TaskDefinition> taskDefinition = taskDefinitionDao.fetchById(taskDefId);
        if (taskDefinition.isPresent()) {
            if (taskDefinition.get().isArchived()) {
                throw new IllegalStateException(String.format("Task definition is already deleted: \"%s\"", taskDefId));
            }
        } else {
            throw new IllegalArgumentException(String.format("Task definition not found: \"%s\"", taskDefId));
        }
        taskDefinitionDao.archive(taskDefId);

        //offline deployed task
        TaskCommit commit = taskCommitService.commit(taskDefId, "OFFLINE");
        try{
            DeployedTask deployedTask = deployedTaskService.find(taskDefId);
            List<Long> commitIds = new ArrayList<>();
            commitIds.add(commit.getId());
            DeployRequest request = new DeployRequest();
            request.setCommitIds(commitIds);
            Deploy deploy = deployService.create(request);
            deployService.publish(deploy.getId());
        }catch (IllegalArgumentException e){
            log.debug("task definition \"{}\" not deployed yet, no need to offline", taskDefId);
        }

    }

    public List<Long> resolveUpstreamTaskDefIds(TaskPayload taskPayload) {
        if (taskPayload == null
                || taskPayload.getScheduleConfig() == null) {
            return Collections.emptyList();
        }
        List<Long> allUpstreamIds = new ArrayList<>();
        List<Long> inputNodes = taskPayload.getScheduleConfig().getInputNodes();
        allUpstreamIds.addAll( inputNodes == null ? ImmutableList.of(): inputNodes);

        List<Long> inputDatasets = Optional.ofNullable(taskPayload.getScheduleConfig()
                .getInputDatasets())
                .map( x -> x.stream()
                        .map(TaskDatasetProps::getDefinitionId)
                        .collect(Collectors.toList()))
                .orElseGet(ImmutableList::of);

        allUpstreamIds.addAll(inputDatasets);
        return allUpstreamIds.stream().distinct().collect(Collectors.toList());
    }

    public TaskTry run(Long taskDefId, TaskRunRequest taskRunRequest) {
        Preconditions.checkNotNull(taskRunRequest);
        TaskDefinition taskDefinition = find(taskDefId);
        TaskTemplate taskTemplate = taskTemplateService.find(taskDefinition.getTaskTemplateName());
        // precheck params
        checkParams(taskTemplate, taskRunRequest.getParameters());
        Long operatorId = taskTemplate.getOperator().getId();

        // build task parameter
        Long creator = getCurrentUser().getId();
        Long taskTryId = DataPlatformIdGenerator.nextTaskTryId();
        // merge default config with request config
        TaskConfig taskConfig = taskTemplateService.getTaskConfig(taskRunRequest.getParameters(), taskTemplate);
        Config config = new Config(taskConfig.getParams());
        Task task = Task.newBuilder()
                .withName(taskTryId.toString())
                .withOperatorId(operatorId)
                .withDescription("Try Run Data Platform Task " + taskTryId)
                .withConfig(config)
                .withTags(TagUtils.buildTryRunTags(creator, taskDefId))
                .withDependencies(new ArrayList<>())
                .withScheduleConf(new ScheduleConf(ScheduleType.NONE, ""))
                .build();

        TaskRun taskRun = workflowClient.executeTask(task,  taskRunRequest.getVariables());
        JSONObject userInputTaskConfig = new JSONObject();
        userInputTaskConfig.put("parameters", taskRunRequest.getParameters());
        userInputTaskConfig.put("variables", taskRunRequest.getVariables());
        TaskTry taskTry = TaskTry
                .newBuilder()
                .withId(taskTryId)
                .withWorkflowTaskId(taskRun.getTask().getId())
                .withWorkflowTaskRunId(taskRun.getId())
                .withTaskConfig(userInputTaskConfig)
                .withCreator(creator)
                .withDefinitionId(taskDefId)
                .build();
        taskTryDao.create(taskTry);
        return taskTry;
    }

    public TaskTry findTaskTry(Long taskTryId) {
        return  taskTryDao.fetchById(taskTryId)
                .<IllegalArgumentException>orElseThrow(() -> {
                    throw new IllegalArgumentException(String.format("Task try not found: \"%s\"", taskTryId));
                });
    }

    public TaskTry stop(Long taskTryId) {
        TaskTry taskTry = findTaskTry(taskTryId);
        workflowClient.stopTaskRun(taskTry.getWorkflowTaskRunId());
        return taskTry;
    }

    public TaskRunLogVO runLog(TaskRunLogRequest  request) {
        TaskRunLog log = workflowClient.getTaskRunLog(request);
        TaskRunStatus status = workflowClient.getTaskRunState(request.getTaskRunId())
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

    public TaskDefinitionVO convertToVO(TaskDefinition taskDefinition) {
        Map<Long, Boolean> commitStatus = taskCommitService.getLatestCommitStatus(
                Collections.singletonList(taskDefinition.getDefinitionId()));
        return mapVO(taskDefinition, commitStatus);
    }

    public List<TaskDefinitionVO> convertToVOList(List<TaskDefinition> taskDefinitions, boolean withTaskPayload) {
        Map<Long, Boolean> commitStatus = taskCommitService.getLatestCommitStatus(
                taskDefinitions.stream()
                        .map(TaskDefinition::getDefinitionId)
                        .collect(Collectors.toList())
        );
        return taskDefinitions.stream()
                .map(taskDefinition -> mapVO(taskDefinition, commitStatus, null, withTaskPayload))
                .collect(Collectors.toList());
    }

    private TaskDefinitionVO mapVO(TaskDefinition taskDefinition, Map<Long, Boolean> commitStatus) {
        List<Long> upstreamDefinitionIds = resolveUpstreamTaskDefIds(taskDefinition.getTaskPayload());
        return mapVO(taskDefinition, commitStatus, taskDefinitionDao.fetchByIds(upstreamDefinitionIds), true);
    }

    private TaskDefinitionVO mapVO(TaskDefinition taskDefinition, Map<Long, Boolean> commitStatus, List<TaskDefinition> taskDefinitions, boolean withTaskPayload) {
        List<TaskDefinitionProps> taskDefinitionProps;
        if (CollectionUtils.isEmpty(taskDefinitions)){
            List<Long> upstreamDefinitionIds = resolveUpstreamTaskDefIds(taskDefinition.getTaskPayload());
            taskDefinitionProps = upstreamDefinitionIds.stream()
                    .map(x -> new TaskDefinitionProps(x, ""))
                    .collect(Collectors.toList());
        } else {
            taskDefinitionProps = taskDefinitions.stream()
                    .map(x -> new TaskDefinitionProps(x.getDefinitionId(), x.getName()))
                    .collect(Collectors.toList());
        }

        return new TaskDefinitionVO(
                taskDefinition.getDefinitionId(),
                taskDefinition.getName(),
                taskDefinition.getTaskTemplateName(),
                withTaskPayload ? taskDefinition.getTaskPayload(): null,
                taskDefinition.getCreator(),
                taskDefinition.isArchived(),
                commitStatus.get(taskDefinition.getDefinitionId()),
                taskDefinition.getOwner(),
                taskDefinitionProps,
                taskDefinition.getLastModifier(),
                taskDefinition.getUpdateTime(),
                taskDefinition.getCreateTime()
        );
    }

    public TaskTryVO convertToTaskTryVO(TaskTry taskTry) {
        TaskRunState taskRun = workflowClient.getTaskRunState(taskTry.getWorkflowTaskRunId());
        return new TaskTryVO(
                taskTry.getId(),
                taskTry.getDefinitionId(),
                taskTry.getWorkflowTaskId(),
                taskTry.getWorkflowTaskRunId(),
                taskTry.getCreator(),
                taskRun.getStatus(),
                taskTry.getTaskConfig()
        );
    }
}
