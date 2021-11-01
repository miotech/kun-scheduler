package com.miotech.kun.dataplatform.web.common.taskdefinition.service;

import com.cronutils.model.Cron;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.miotech.kun.dataplatform.facade.TaskDefinitionFacade;
import com.miotech.kun.dataplatform.facade.model.commit.TaskCommit;
import com.miotech.kun.dataplatform.facade.model.deploy.Deploy;
import com.miotech.kun.dataplatform.facade.model.deploy.DeployedTask;
import com.miotech.kun.dataplatform.facade.model.taskdefinition.*;
import com.miotech.kun.dataplatform.web.common.commit.service.TaskCommitService;
import com.miotech.kun.dataplatform.web.common.datastore.service.DatasetService;
import com.miotech.kun.dataplatform.web.common.deploy.service.DeployService;
import com.miotech.kun.dataplatform.web.common.deploy.service.DeployedTaskService;
import com.miotech.kun.dataplatform.web.common.deploy.vo.DeployRequest;
import com.miotech.kun.dataplatform.web.common.taskdefinition.dao.TaskDefinitionDao;
import com.miotech.kun.dataplatform.web.common.taskdefinition.dao.TaskRelationDao;
import com.miotech.kun.dataplatform.web.common.taskdefinition.dao.TaskTryDao;
import com.miotech.kun.dataplatform.web.common.taskdefinition.vo.*;
import com.miotech.kun.dataplatform.web.common.taskdefview.dao.TaskDefinitionViewDao;
import com.miotech.kun.dataplatform.web.common.tasktemplate.service.TaskTemplateService;
import com.miotech.kun.dataplatform.web.common.utils.DataPlatformIdGenerator;
import com.miotech.kun.dataplatform.web.common.utils.TagUtils;
import com.miotech.kun.dataplatform.web.model.tasktemplate.ParameterDefinition;
import com.miotech.kun.dataplatform.web.model.tasktemplate.TaskTemplate;
import com.miotech.kun.monitor.facade.alert.TaskNotifyConfigFacade;
import com.miotech.kun.security.service.BaseSecurityService;
import com.miotech.kun.workflow.client.WorkflowClient;
import com.miotech.kun.workflow.client.model.*;
import com.miotech.kun.workflow.core.execution.Config;
import com.miotech.kun.workflow.core.model.common.Tag;
import com.miotech.kun.workflow.core.model.task.ScheduleConf;
import com.miotech.kun.workflow.core.model.task.ScheduleType;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunStatus;
import com.miotech.kun.workflow.utils.CronUtils;
import com.miotech.kun.workflow.utils.DateTimeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jgrapht.alg.cycle.CycleDetector;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.jgrapht.traverse.TopologicalOrderIterator;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.jgrapht.*;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

import static com.miotech.kun.monitor.facade.model.alert.TaskDefNotifyConfig.DEFAULT_TASK_NOTIFY_CONFIG;


@Service
@Slf4j
@Scope(proxyMode = ScopedProxyMode.TARGET_CLASS)
public class TaskDefinitionService extends BaseSecurityService implements TaskDefinitionFacade {

    private static final List<ScheduleType> VALID_SCHEDULE_TYPE = ImmutableList.of(ScheduleType.SCHEDULED, ScheduleType.ONESHOT, ScheduleType.NONE);

    private static final Long devTargetId = 2L; //TODO edit in prod version

    @Autowired
    private TaskDefinitionDao taskDefinitionDao;

    @Autowired
    private TaskRelationDao taskRelationDao;

    @Autowired
    private TaskDefinitionViewDao taskDefinitionViewDao;

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
    private TaskNotifyConfigFacade taskNotifyConfigFacade;

    @Autowired
    @Lazy
    private WorkflowClient workflowClient;

    public TaskDefinition find(Long taskDefId) {
        return taskDefinitionDao.fetchById(taskDefId)
                .<IllegalArgumentException>orElseThrow(() -> {
                    throw new NoSuchElementException(String.format("Task definition not found: \"%s\"", taskDefId));
                });
    }

    /**
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

        //Check task name duplicate
        checkName(taskName, null);

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
                .withNotifyConfig(DEFAULT_TASK_NOTIFY_CONFIG)
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

        //Check task name duplicate
        String taskName = request.getName();
        checkName(taskName, definitionId);
        
        TaskPayload taskPayload = request.getTaskPayload();
        validateSchedule(taskPayload.getScheduleConfig());
        //validate payload
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

        List<Long> upstream = new ArrayList<>();
        upstream.addAll(updated.getTaskPayload().getScheduleConfig().getInputNodes());
        upstream.addAll(updated.getTaskPayload().getScheduleConfig().getInputDatasets()
                .stream()
                .map(x -> x.getDefinitionId())
                .distinct()
                .collect(Collectors.toList())
        );

        if (!upstream.isEmpty()) {
            taskRelationDao.delete(definitionId);
            List<TaskRelation> taskRelations = upstream.stream().map(x -> new TaskRelation(x, definitionId, DateTimeUtils.now(), DateTimeUtils.now())).collect(Collectors.toList());
            taskRelationDao.create(taskRelations);
        }

        // Update deployed task notification configuration if presented
        Optional<DeployedTask> deployedTaskOptional = deployedTaskService.findOptional(taskDefinition.getDefinitionId());
        deployedTaskOptional.ifPresent(deployedTask -> taskNotifyConfigFacade.updateRelatedTaskNotificationConfig(
                deployedTask.getWorkflowTaskId(),
                taskDefinition.getTaskPayload().getNotifyConfig()
        ));

        return updated;
    }


    private void checkName(String taskName, @Nullable Long taskDefinitionId) {
        List<TaskDefinition> taskDefinitionsToCheck = taskDefinitionDao.fetchAliveTaskDefinitionByName(taskName);
        Optional<TaskDefinition> taskDefinitionToCheck = taskDefinitionId == null? taskDefinitionsToCheck.stream()
                .findAny() : taskDefinitionsToCheck.stream()
                .filter(taskDef -> !taskDef.getDefinitionId().equals(taskDefinitionId))
                .findAny();

        if (taskDefinitionToCheck.isPresent()) {
            throw new IllegalArgumentException(String.format("task definition name already exists: \"%s\"", taskName));
        }
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
            for (String key : requiredParameterNames) {
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
        Preconditions.checkNotNull(scheduleConfig.getType(), "Schedule type should not be null");
        ScheduleType scheduleType = ScheduleType.valueOf(scheduleConfig.getType());
        Preconditions.checkArgument(VALID_SCHEDULE_TYPE.contains(scheduleType),
                "Schedule type should be one of the followings: " + VALID_SCHEDULE_TYPE.stream()
                        .map(ScheduleType::name)
                        .collect(Collectors.joining(",")));
        if (!Objects.equals(scheduleType, ScheduleType.NONE)) {
            Preconditions.checkArgument(StringUtils.isNoneBlank(scheduleConfig.getCronExpr()),
                    "Cron Expression should not be blank");
        }
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

        List<TaskRelation> downStreamTaskDefIds = taskRelationDao.fetchByUpstreamId(taskDefId);
        if (!downStreamTaskDefIds.isEmpty()) {
            throw new RuntimeException(String.format("Task definition has downStream dependencies, please update downStream task definition first, \"%s\"", taskDefId));
        }
        List<Long> downStreamWorkflowTaskIds = deployedTaskService.getDownStreamWorkflowTasks(taskDefId);
        if (!downStreamWorkflowTaskIds.isEmpty()) {
            throw new RuntimeException(String.format("Task definition \"%s\" has deployed downStream dependencies, please update downStream task and deploy first", taskDefId));
        }

        taskDefinitionDao.archive(taskDefId);
        taskRelationDao.delete(taskDefId);

        //offline deployed task
        TaskCommit commit = taskCommitService.commit(taskDefId, "OFFLINE");
        try {
            DeployedTask deployedTask = deployedTaskService.find(taskDefId);
            List<Long> commitIds = new ArrayList<>();
            commitIds.add(commit.getId());
            DeployRequest request = new DeployRequest();
            request.setCommitIds(commitIds);
            Deploy deploy = deployService.create(request);
            deployService.publish(deploy.getId());
        } catch (IllegalArgumentException e) {
            log.debug("task definition \"{}\" not deployed yet, no need to offline", taskDefId);
        }

        // Remove all relations from target task definition views
        taskDefinitionViewDao.removeAllRelationsOfTaskDefinition(taskDefId);
    }

    public List<Long> resolveUpstreamTaskDefIds(TaskPayload taskPayload) {
        if (taskPayload == null
                || taskPayload.getScheduleConfig() == null) {
            return Collections.emptyList();
        }
        List<Long> allUpstreamIds = new ArrayList<>();
        List<Long> inputNodes = taskPayload.getScheduleConfig().getInputNodes();
        allUpstreamIds.addAll(inputNodes == null ? ImmutableList.of() : inputNodes);

        List<Long> inputDatasets = Optional.ofNullable(taskPayload.getScheduleConfig()
                .getInputDatasets())
                .map(x -> x.stream()
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
        TaskConfig taskConfig = taskTemplateService.getTaskConfig(taskRunRequest.getParameters(), taskTemplate, taskDefinition);
        Config config = new Config(taskConfig.getParams());
        Task task = Task.newBuilder()
                .withName(taskDefinition.getName() + "_TryRun_" + taskDefId.toString())
                .withOperatorId(operatorId)
                .withDescription("Try Run Data Platform Task " + taskDefId)
                .withConfig(config)
                .withTags(TagUtils.buildTryRunTags(creator, taskDefId))
                .withDependencies(new ArrayList<>())
                .withScheduleConf(new ScheduleConf(ScheduleType.NONE, ""))
                .build();
        TaskRun taskRun = workflowClient.executeTask(task, taskRunRequest.getVariables(), devTargetId);
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




    public List<TaskTry> runBatch(TaskTryBatchRequest taskTryBatchRequest) {
        Preconditions.checkNotNull(taskTryBatchRequest);

        Graph<TaskDefinition, DefaultEdge> graph = buildTaskRunBatchGraph(taskTryBatchRequest);

        BiMap<TaskDefinition, Task> taskDefToTaskBiMap = saveTaskInTopoOrder(graph);

        RunTaskRequest runTaskRequest = new RunTaskRequest();
        taskDefToTaskBiMap.values().stream()
                .map(Task::getId)
                .forEach(x -> runTaskRequest.addTaskConfig(x, Maps.newHashMap()));
        runTaskRequest.setTargetId(devTargetId);

        //Map<Long, TaskRun> taskIdToTaskRunMap = workflowClient.executeTasks(runTaskRequest, devTargetId);
        Map<Long, TaskRun> taskIdToTaskRunMap = workflowClient.executeTasks(runTaskRequest);

        List<TaskTry> taskTryList = persistTaskTry(taskIdToTaskRunMap, taskDefToTaskBiMap);

        return taskTryList;
    }

    @VisibleForTesting
    Graph<TaskDefinition, DefaultEdge> buildTaskRunBatchGraph(TaskTryBatchRequest taskTryBatchRequest) {
        List<TaskDefinition> taskDefList = taskDefinitionDao.fetchByIds(taskTryBatchRequest.getIdList());
        List<Long> taskDefIdList = taskDefList.stream().map(TaskDefinition::getDefinitionId).collect(Collectors.toList());
        Map<TaskDefinition, List<TaskDefinition>> taskDefWithDependenciesMap = new HashMap<>();
        for (TaskDefinition taskDef : taskDefList) {
            //获取包含在当前批量试运行中的上游任务id
            List<Long> upstreamTaskDefIdList = resolveUpstreamTaskDefIds(taskDef.getTaskPayload()).stream()
                    .filter(taskDefIdList::contains)
                    .collect(Collectors.toList());

            taskDefWithDependenciesMap.put(taskDef,
                    taskDefList.stream()
                            .filter(x -> upstreamTaskDefIdList.contains(x.getDefinitionId()))
                            .collect(Collectors.toList()));
        }

        //建立task def graph
        Graph<TaskDefinition, DefaultEdge> graph = new SimpleDirectedGraph<>(DefaultEdge.class);
        taskDefList.forEach(graph::addVertex);
        for (TaskDefinition taskDef : taskDefWithDependenciesMap.keySet()) {
            List<TaskDefinition> upstreamTaskDefList = taskDefWithDependenciesMap.get(taskDef);
            upstreamTaskDefList.forEach(x -> graph.addEdge(x, taskDef));
        }
        return graph;
    }

    private BiMap<TaskDefinition, Task> saveTaskInTopoOrder(Graph<TaskDefinition, DefaultEdge> graph) {
        //判断是否存在循环依赖 (是否有环)
        CycleDetector<TaskDefinition, DefaultEdge> cycleDetector = new CycleDetector<>(graph);
        if (cycleDetector.detectCycles()) {
            throw new RuntimeException("Task try run batch has circular dependence");
        }

        BiMap<TaskDefinition, Task> taskDefToTaskBiMap = HashBiMap.create();

        Long creator = getCurrentUser().getId();

        //按拓扑排序结果依次saveTask()获取TaskID
        TopologicalOrderIterator<TaskDefinition, DefaultEdge> iterator = new TopologicalOrderIterator<>(graph);
        while (iterator.hasNext()) {
            TaskDefinition taskDef = iterator.next();
            Long taskDefId = taskDef.getDefinitionId();

            TaskTemplate taskTemplate = taskTemplateService.find(taskDef.getTaskTemplateName());
            checkParams(taskTemplate, taskDef.getTaskPayload().getTaskConfig());
            Long operatorId = taskTemplate.getOperator().getId();

            TaskConfig taskConfig = taskTemplateService.getTaskConfig(taskDef.getTaskPayload().getTaskConfig(), taskTemplate.getName(), taskDef);
            Config config = new Config(taskConfig.getParams());

            List<TaskDependency> dependencies = graph.incomingEdgesOf(taskDef).stream()
                    .map(graph::getEdgeSource)
                    .map(taskDefToTaskBiMap::get).filter(Objects::nonNull)
                    .map(Task::getId)
                    .map(x -> new TaskDependency(x, "taskDependency"))
                    .distinct()
                    .collect(Collectors.toList());

            Task task = Task.newBuilder()
                    .withName(taskDef.getName() +  "_TryRun_" + taskDefId.toString())
                    .withOperatorId(operatorId)
                    .withDescription("Try Run Data Platform Task " + taskDefId)
                    .withConfig(config)
                    .withTags(TagUtils.buildTryRunTags(creator, taskDefId))
                    .withDependencies(dependencies)
                    .withScheduleConf(new ScheduleConf(ScheduleType.NONE, ""))
                    .build();
            List<Tag> searchTags = TagUtils.buildTryRunSearchTags(taskDefId, taskTemplate.getName());

            task = workflowClient.saveTask(task, searchTags);
            taskDefToTaskBiMap.put(taskDef, task);
        }
        return taskDefToTaskBiMap;
    }

    private List<TaskTry> persistTaskTry(Map<Long, TaskRun> taskIdToTaskRunMap, BiMap<TaskDefinition, Task> taskDefToTaskBiMap) {
        Long creator = getCurrentUser().getId();
        List<TaskTry> taskTryList = new ArrayList<>();
        for (TaskRun taskRun : taskIdToTaskRunMap.values()) {
            Long taskTryId = DataPlatformIdGenerator.nextTaskTryId();
            Long taskDefId = taskDefToTaskBiMap.inverse().get(taskRun.getTask()).getDefinitionId();
            TaskDefinition taskDef = taskDefToTaskBiMap.keySet().stream()
                    .filter(x -> x.getDefinitionId().equals(taskDefId))
                    .findFirst().get();
            JSONObject taskConfig = new JSONObject(taskDef.getTaskPayload().getTaskConfig());
            TaskTry taskTry = TaskTry.newBuilder()
                    .withId(taskTryId)
                    .withWorkflowTaskId(taskRun.getTask().getId())
                    .withWorkflowTaskRunId(taskRun.getId())
                    .withTaskConfig(taskConfig)
                    .withCreator(creator)
                    .withDefinitionId(taskDefId)
                    .build();
            taskTryDao.create(taskTry);
            taskTryList.add(taskTry);
        }
        return taskTryList;
    }



    public TaskTry findTaskTry(Long taskTryId) {
        return taskTryDao.fetchById(taskTryId)
                .<IllegalArgumentException>orElseThrow(() -> {
                    throw new IllegalArgumentException(String.format("Task try not found: \"%s\"", taskTryId));
                });
    }

    public Optional<TaskTry> findTaskTryByTaskRunId(Long taskRunId) {
        return taskTryDao.fetchByTaskRunId(taskRunId);
    }

    public TaskTry stop(Long taskTryId) {
        TaskTry taskTry = findTaskTry(taskTryId);
        workflowClient.stopTaskRun(taskTry.getWorkflowTaskRunId());
        return taskTry;
    }

    public void stopBatch(TaskTryBatchRequest taskTryBatchRequest) {
        List<Long> taskTryIdList = taskTryBatchRequest.getIdList();
        List<TaskTry> taskTryList = taskTryDao.fetchByIds(taskTryIdList);
        workflowClient.stopTaskRuns(taskTryList.stream()
                .map(TaskTry::getWorkflowTaskRunId)
                .collect(Collectors.toList()));
    }

    public TaskRunLogVO runLog(TaskRunLogRequest request) {
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
        if (CollectionUtils.isEmpty(taskDefinitions)) {
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
                withTaskPayload ? taskDefinition.getTaskPayload() : null,
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

    public List<TaskTryVO> convertToTaskTryVOList(List<TaskTry> taskTryList) {
        return taskTryList.stream().map(this::convertToTaskTryVO).collect(Collectors.toList());
    }

    private void validateSchedule(ScheduleConfig conf) {
        if (!conf.getType().equals("NONE")) {
            Cron cron = CronUtils.convertStringToCron(conf.getCronExpr());
            CronUtils.validateCron(cron);
        }
    }
}
