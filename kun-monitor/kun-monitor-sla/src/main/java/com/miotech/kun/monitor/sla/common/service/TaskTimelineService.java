package com.miotech.kun.monitor.sla.common.service;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
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

import java.util.Collections;
import java.util.Comparator;
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
        log.debug("The last 7 successful taskruns: {}", JSONUtils.toJsonString(latestTaskRuns));
        int runTime = calculateLatestTaskRunTimeAverage(latestTaskRuns);
        slaService.updateAvgTaskRunTimeLastSevenTimes(definitionId, runTime);

        // find all downstream paths with `deadline` config
        List<List<TaskDefinitionNode>> taskDefinitionNodes = slaService.findDownstreamPathHasSlaConfig(definitionId);
        log.debug("All paths leading to the child nodes with sla config: {}", JSONUtils.toJsonString(taskDefinitionNodes));
        // build backtracking timeline
        Integer maxLevel = calculateBacktrackingMaxLevel(taskDefinitionNodes);
        log.debug("maxLevel: {}", maxLevel);
        buildBacktrackingTimeline(event, definitionId, taskDefinitionNodes, maxLevel);

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
            log.debug("Create taskTimeline: {}", JSONUtils.toJsonString(taskTimeline));
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

        double avg = latestTaskRuns.stream()
                .filter(taskRun -> taskRun.getQueuedAt() != null && taskRun.getEndAt() != null)
                .map(taskRun -> taskRun.getEndAt().toEpochSecond() - taskRun.getQueuedAt().toEpochSecond())
                .mapToLong(second -> second)
                .average()
                .orElse(0);

        return (int) avg / 60;
    }

    private void buildBacktrackingTimeline(TaskRunCreatedEvent event, Long definitionId, List<List<TaskDefinitionNode>> taskDefinitionNodes,
                                           Integer maxLevel) {
        List<TaskTimeline> taskTimelines = Lists.newArrayList();
        for (List<TaskDefinitionNode> path : taskDefinitionNodes) {
            BacktrackingSlaConfig backtrackingSlaConfig = calculateBacktrackingSlaConfigOfPath(path);
            TaskTimeline taskTimeline = buildAssociatedTimeline(event, definitionId, backtrackingSlaConfig);
            taskTimelines.add(taskTimeline);
        }

        if (CollectionUtils.isNotEmpty(taskTimelines)) {
            TaskTimeline minTaskTimeline = Collections.min(taskTimelines, Comparator.comparing(TaskTimeline::getDeadline));
            minTaskTimeline = minTaskTimeline.cloneBuilder().withLevel(maxLevel).build();
            log.debug("Create backtracking taskTimeline: {}", JSONUtils.toJsonString(minTaskTimeline));
            taskTimelineDao.create(minTaskTimeline);
        }
    }

    private TaskTimeline buildAssociatedTimeline(TaskRunCreatedEvent event, Long definitionId, BacktrackingSlaConfig backtrackingSlaConfig) {
        return TaskTimeline.newBuilder()
                .withTaskRunId(event.getTaskRunId())
                .withDefinitionId(definitionId)
                .withLevel(backtrackingSlaConfig.getMaxLevel())
                .withDeadline(backtrackingSlaConfig.getDeadline())
                .withRootDefinitionId(backtrackingSlaConfig.getRootDefinitionId())
                .withCreatedAt(DateTimeUtils.now())
                .withUpdatedAt(DateTimeUtils.now())
                .build();
    }

    private Integer calculateBacktrackingMaxLevel(List<List<TaskDefinitionNode>> taskDefinitionNodes) {
        Integer maxLevel = null;
        for (List<TaskDefinitionNode> path : taskDefinitionNodes) {
            BacktrackingSlaConfig backtrackingSlaConfigOfPath = calculateBacktrackingSlaConfigOfPath(path);
            if (maxLevel == null) {
                maxLevel = backtrackingSlaConfigOfPath.getMaxLevel();
                continue;
            }

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
        for (int i = path.size() - 1; i > 0; i--) {
            TaskDefinitionNode taskDefinitionNode = path.get(i);
            if (i == path.size() - 1) {
                deadline = taskDefinitionNode.getDeadline();
                level = taskDefinitionNode.getLevel();
                rootDefinitionId = taskDefinitionNode.getId();
            }

            if (deadline == null) {
                break;
            }

            if (taskDefinitionNode.getAvgTaskRunTimeLastSevenTimes() == null) {
                log.warn("TaskDefinition: {} runTime is null", taskDefinitionNode.getId());
                continue;
            }

            deadline = deadline - taskDefinitionNode.getAvgTaskRunTimeLastSevenTimes();
        }

        return new BacktrackingSlaConfig(level, deadline, rootDefinitionId);
    }

}
