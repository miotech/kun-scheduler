package com.miotech.kun.monitor.sla.common.service;

import com.google.common.base.Preconditions;
import com.miotech.kun.common.utils.JSONUtils;
import com.miotech.kun.dataplatform.facade.DeployedTaskFacade;
import com.miotech.kun.dataplatform.facade.TaskDefinitionFacade;
import com.miotech.kun.dataplatform.facade.model.deploy.DeployedTask;
import com.miotech.kun.dataplatform.facade.model.taskdefinition.ScheduleConfig;
import com.miotech.kun.dataplatform.facade.model.taskdefinition.TaskDefinition;
import com.miotech.kun.monitor.sla.common.dao.TaskTimelineDao;
import com.miotech.kun.monitor.sla.model.TaskTimeline;
import com.miotech.kun.monitor.facade.model.sla.SlaConfig;
import com.miotech.kun.workflow.client.WorkflowClient;
import com.miotech.kun.workflow.core.event.TaskRunCreatedEvent;
import com.miotech.kun.workflow.utils.DateTimeUtils;
import lombok.extern.slf4j.Slf4j;
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
    private TaskDefinitionFacade taskDefinitionFacade;

    @Autowired
    private WorkflowClient workflowClient;

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

        Long definitionId = deployedTaskOpt.get().getDefinitionId();
        TaskDefinition taskDefinition = taskDefinitionFacade.find(definitionId);
        ScheduleConfig scheduleConfig = taskDefinition.getTaskPayload().getScheduleConfig();
        SlaConfig slaConfig = scheduleConfig.getSlaConfig();
        if (slaConfig == null || (slaConfig.getHours() == null && slaConfig.getMinutes() == null)) {
            return;
        }

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

        // update task-run priority
        log.debug("Prepare to change task-run priority, taskRunId: {}, priority: {}", event.getTaskRunId(), slaConfig.getPriority());
        Boolean changeTaskRunPriority = workflowClient.changeTaskRunPriority(event.getTaskRunId(), slaConfig.getPriority());
        log.debug("Change task-run priority result: {}", changeTaskRunPriority);
    }

}
