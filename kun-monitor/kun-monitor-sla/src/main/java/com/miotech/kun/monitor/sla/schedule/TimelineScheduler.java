package com.miotech.kun.monitor.sla.schedule;

import com.miotech.kun.dataplatform.facade.DeployedTaskFacade;
import com.miotech.kun.dataplatform.facade.model.deploy.DeployedTask;
import com.miotech.kun.monitor.sla.common.service.TaskTimelineService;
import com.miotech.kun.monitor.sla.model.TaskTimeline;
import com.miotech.kun.monitor.sla.utils.LocalDateTimeUtils;
import com.miotech.kun.monitor.facade.alert.NotifyFacade;
import com.miotech.kun.workflow.client.WorkflowClient;
import com.miotech.kun.workflow.client.model.TaskRunState;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Slf4j
@Component
public class TimelineScheduler {

    private static final String MSG_TEMPLATE = "Deployed task: '%s' not completed before the deadline.%nRoot definition id:%s%n%nSee link: %s";

    @Value("${notify.urlLink.prefix}")
    private String prefix;

    @Autowired
    private TaskTimelineService taskTimelineService;

    @Autowired
    private WorkflowClient workflowClient;

    @Autowired(required = false)
    private NotifyFacade notifyFacade;

    @Autowired
    private DeployedTaskFacade deployedTaskFacade;

    @Async
    @Scheduled(cron = "0 0/1 * * * ?")
    public void execute() {
        log.debug("TimelineScheduler start execution");
        String nowDateTime = LocalDateTimeUtils.format();
        log.debug("Argument nowDateTime: {}", nowDateTime);
        List<TaskTimeline> taskTimelines = taskTimelineService.fetchByDeadline(nowDateTime);
        if (CollectionUtils.isEmpty(taskTimelines)) {
            return;
        }

        log.debug("The number of tasks that need to be checked is {}", taskTimelines.size());
        for (TaskTimeline taskTimeline : taskTimelines) {
            TaskRunState taskRunState = workflowClient.getTaskRunState(taskTimeline.getTaskRunId());
            if (taskRunState.getStatus().isSuccess()) {
                continue;
            }

            Long definitionId = taskTimeline.getDefinitionId();
            Optional<DeployedTask> deployedTaskOpt = deployedTaskFacade.findOptional(definitionId);
            if (!deployedTaskOpt.isPresent()) {
                log.warn("The corresponding DeployedTask cannot be found according to the definitionId: {}", definitionId);
                continue;
            }

            DeployedTask deployedTask = deployedTaskOpt.get();
            String msg = String.format(MSG_TEMPLATE, deployedTask.getName(), taskTimeline.getRootDefinitionId(), this.prefix + String.format("/operation-center/scheduled-tasks/%s?taskRunId=%s", definitionId, taskTimeline.getTaskRunId()));
            log.debug("Task: {} is not executed before the deadline, send an alarm, msg: {}", deployedTask.getName(), msg);
            notifyFacade.notify(deployedTask.getWorkflowTaskId(), msg);
        }
    }

}
