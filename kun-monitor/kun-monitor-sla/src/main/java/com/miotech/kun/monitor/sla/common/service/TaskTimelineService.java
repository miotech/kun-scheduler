package com.miotech.kun.monitor.sla.common.service;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.miotech.kun.common.utils.JSONUtils;
import com.miotech.kun.dataplatform.facade.DeployedTaskFacade;
import com.miotech.kun.dataplatform.facade.model.deploy.DeployedTask;
import com.miotech.kun.dataplatform.facade.model.taskdefinition.ScheduleConfig;
import com.miotech.kun.monitor.facade.model.sla.SlaConfig;
import com.miotech.kun.monitor.facade.model.sla.TaskDefinitionNode;
import com.miotech.kun.monitor.sla.common.dao.TaskTimelineDao;
import com.miotech.kun.monitor.sla.model.BacktrackingSlaConfig;
import com.miotech.kun.monitor.sla.model.TaskTimeline;
import com.miotech.kun.workflow.client.WorkflowClient;
import com.miotech.kun.workflow.client.model.TaskRun;
import com.miotech.kun.workflow.core.event.TaskRunCreatedEvent;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunStatus;
import com.miotech.kun.workflow.utils.DateTimeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class TaskTimelineService {

    @Autowired
    private TaskTimelineDao taskTimelineDao;

    @Autowired
    private DeployedTaskFacade deployedTaskFacade;

    @Autowired
    private WorkflowClient workflowClient;

    @Autowired
    private SlaService slaService;

    public List<TaskTimeline> fetchByDeadline(String deadline) {
        return taskTimelineDao.fetchByDeadline(deadline);
    }

    public void create(TaskTimeline taskTimeline) {
        taskTimelineDao.create(taskTimeline);
    }

    public void handleTaskRunCreatedEvent(TaskRunCreatedEvent event) {
        log.debug("Receive event: {}", JSONUtils.toJsonString(event));
        Preconditions.checkNotNull(event, "Argument `event` should not be null");
        Optional<DeployedTask> deployedTaskOpt = deployedTaskFacade.findByWorkflowTaskId(event.getTaskId());
        if (!deployedTaskOpt.isPresent()) {
            log.warn("The corresponding DeployedTask cannot be found according to the taskId: {}", event.getTaskId());
            return;
        }

        DeployedTask deployedTask = deployedTaskOpt.get();
        Long definitionId = deployedTask.getDefinitionId();

        // update task run-time in neo4j
        List<TaskRun> latestTaskRuns = workflowClient.getLatestTaskRuns(deployedTask.getWorkflowTaskId(), ImmutableList.of(TaskRunStatus.SUCCESS), 7);
        int runTime = calculateLatestTaskRunTimeAverage(latestTaskRuns);
        slaService.updateRunTime(definitionId, runTime);

        // find all downstream paths with `deadline` config
        List<List<TaskDefinitionNode>> taskDefinitionNodes = slaService.findDownstreamPathHasSlaConfig(definitionId);
        // build backtracking timeline
        buildBacktrackingTimeline(event, definitionId, taskDefinitionNodes);
        Integer maxLevel = calculateBacktrackingMaxLevel(taskDefinitionNodes);

        ScheduleConfig scheduleConfig = deployedTask.getTaskCommit().getSnapshot().getTaskPayload().getScheduleConfig();
        SlaConfig slaConfig = scheduleConfig.getSlaConfig();

        if (slaConfig != null && slaConfig.getHours() != null) {
            // build timeline
            TaskTimeline taskTimeline = TaskTimeline.newBuilder()
                    .withTaskRunId(event.getTaskRunId())
                    .withDefinitionId(definitionId)
                    .withLevel(slaConfig.getLevel())
                    .withDeadline(slaConfig.getDeadline(scheduleConfig.getTimeZone()))
                    .withRootDefinitionId(null)
                    .withCreatedAt(DateTimeUtils.now())
                    .withUpdatedAt(DateTimeUtils.now())
                    .build();
            taskTimelineDao.create(taskTimeline);
        }

        // update task-run priority
        Integer priority = calculatePriority(maxLevel, slaConfig);
        if (priority != null) {
            log.debug("Prepare to change task-run priority, taskRunId: {}, priority: {}", event.getTaskRunId(), priority);
            Boolean changeTaskRunPriority = workflowClient.changeTaskRunPriority(event.getTaskRunId(), priority);
            log.debug("Change task-run priority result: {}", changeTaskRunPriority);
        }
    }

    private Integer calculatePriority(Integer maxLevel, SlaConfig slaConfig) {
        if (slaConfig == null || slaConfig.getLevel() == null) {
            return maxLevel;
        }

        if (maxLevel == null) {
            return slaConfig.getLevel();
        }

        return Math.max(slaConfig.getPriority(), maxLevel);
    }

    private int calculateLatestTaskRunTimeAverage(List<TaskRun> latestTaskRuns) {
        if (CollectionUtils.isEmpty(latestTaskRuns)) {
            return 0;
        }

        double avg = latestTaskRuns.stream().map(taskRun -> taskRun.getEndAt().toEpochSecond() - taskRun.getQueuedAt().toEpochSecond())
                .mapToLong(second -> second)
                .average()
                .orElse(0);

        return (int) avg / 60;
    }

    private void buildBacktrackingTimeline(TaskRunCreatedEvent event, Long definitionId, List<List<TaskDefinitionNode>> taskDefinitionNodes) {
        for (List<TaskDefinitionNode> path : taskDefinitionNodes) {
            BacktrackingSlaConfig backtrackingSlaConfig = calculateBacktrackingSlaConfigOfPath(path);
            buildAssociatedTimeline(event, definitionId, backtrackingSlaConfig);
        }
    }

    private void buildAssociatedTimeline(TaskRunCreatedEvent event, Long definitionId, BacktrackingSlaConfig backtrackingSlaConfig) {
        TaskTimeline taskTimeline = TaskTimeline.newBuilder()
                .withTaskRunId(event.getTaskRunId())
                .withDefinitionId(definitionId)
                .withLevel(backtrackingSlaConfig.getMaxLevel())
                .withDeadline(backtrackingSlaConfig.getDeadline())
                .withRootDefinitionId(backtrackingSlaConfig.getRootDefinitionId())
                .withCreatedAt(DateTimeUtils.now())
                .withUpdatedAt(DateTimeUtils.now())
                .build();
        taskTimelineDao.create(taskTimeline);
    }

    private Integer calculateBacktrackingMaxLevel(List<List<TaskDefinitionNode>> taskDefinitionNodes) {
        Integer maxLevel = null;
        for (List<TaskDefinitionNode> path : taskDefinitionNodes) {
            BacktrackingSlaConfig backtrackingSlaConfigOfPath = calculateBacktrackingSlaConfigOfPath(path);
            if (backtrackingSlaConfigOfPath.getMaxLevel() != null &&
                    backtrackingSlaConfigOfPath.getMaxLevel() > maxLevel) {
                maxLevel = backtrackingSlaConfigOfPath.getMaxLevel();
            }
        }

        return maxLevel;
    }

    private BacktrackingSlaConfig calculateBacktrackingSlaConfigOfPath(List<TaskDefinitionNode> path) {
        Integer deadline = null;
        Long rootDefinitionId = null;
        Integer level = null;
        for (int i = path.size() - 1; i >= 0; i--) {
            TaskDefinitionNode taskDefinitionNode = path.get(i);
            if (i == path.size() - 1) {
                deadline = taskDefinitionNode.getDeadline();
                level = taskDefinitionNode.getLevel();
                rootDefinitionId = taskDefinitionNode.getId();
            }

            if (deadline == null) {
                break;
            }

            if (taskDefinitionNode.getRunTime() == null) {
                log.warn("TaskDefinition: {} runTime is null", taskDefinitionNode.getId());
                continue;
            }

            deadline = deadline - taskDefinitionNode.getRunTime();
        }

        return new BacktrackingSlaConfig(level, deadline, rootDefinitionId);
    }

}
