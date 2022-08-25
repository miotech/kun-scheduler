package com.miotech.kun.monitor.sla.schedule;

import com.miotech.kun.dataplatform.facade.DeployedTaskFacade;
import com.miotech.kun.dataplatform.facade.TaskDefinitionFacade;
import com.miotech.kun.dataplatform.facade.model.deploy.DeployedTask;
import com.miotech.kun.dataplatform.facade.model.taskdefinition.TaskDefinition;
import com.miotech.kun.monitor.facade.alert.NotifyFacade;
import com.miotech.kun.monitor.facade.model.alert.AlertMessage;
import com.miotech.kun.monitor.sla.common.service.TaskTimelineService;
import com.miotech.kun.monitor.sla.model.TaskTimeline;
import com.miotech.kun.monitor.sla.utils.LocalDateTimeUtils;
import com.miotech.kun.security.model.UserInfo;
import com.miotech.kun.workflow.client.WorkflowClient;
import com.miotech.kun.workflow.client.model.TaskRun;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunStatus;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
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

    @Autowired
    private TaskDefinitionFacade taskDefinitionFacade;

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
            TaskRun taskRun = workflowClient.getTaskRun(taskTimeline.getTaskRunId());
            if (taskRun.getStatus().isSuccess()) {
                continue;
            }

            Long definitionId = taskTimeline.getDefinitionId();
            Optional<DeployedTask> deployedTaskOpt = deployedTaskFacade.findOptional(definitionId);
            if (!deployedTaskOpt.isPresent()) {
                log.warn("The corresponding DeployedTask cannot be found according to the definitionId: {}", definitionId);
                continue;
            }

            DeployedTask deployedTask = deployedTaskOpt.get();
            String rootDefinitionName = fetchTaskDefinitionName(taskTimeline.getRootDefinitionId());
            UserInfo userInfo = deployedTaskFacade.getUserByTaskId(deployedTask.getWorkflowTaskId());
            String result = buildResult(rootDefinitionName);
            String link = this.prefix + String.format("/operation-center/scheduled-tasks/%s?taskRunId=%s", definitionId, taskTimeline.getTaskRunId());
            AlertMessage alertMessage = AlertMessage.newBuilder()
                    .withReason(AlertMessage.AlertReason.OVERDUE)
                    .withTask(deployedTask.getName())
                    .withResult(result)
                    .withOwner(userInfo.getUsername())
                    .withLink(link)
                    .build();
            String msg = alertMessage.toMarkdown();
            StringBuilder extraInfoSb = new StringBuilder();
            if (taskRun.getStatus().isUpstreamFailed()) {
                extraInfoSb.append(String.format("%n[task status]: %s", TaskRunStatus.UPSTREAM_FAILED.name()));
                extraInfoSb.append("\n[root cause]:");
                for (TaskRun failedUpstreamTaskRun : taskRun.getFailedUpstreamTaskRuns()) {
                    extraInfoSb.append(String.format("%n-[task]:%s, [link]:%s", failedUpstreamTaskRun.getTask().getName(),
                            this.prefix+"/operation-center/task-run-id/"+failedUpstreamTaskRun.getId()));
                }
            }
            msg = msg + extraInfoSb.toString();
            log.debug("Task: {} is not executed before the deadline, send an alarm, msg: {}", deployedTask.getName(), msg);
            notifyFacade.notify(deployedTask.getWorkflowTaskId(), "SLA ALERT", msg);
        }
    }

    private String buildResult(String rootDefinitionName) {
        if (StringUtils.isBlank(rootDefinitionName)) {
            return "not finished before deadline";
        }

        return String.format("caused by downstream task: %s upward forecast", rootDefinitionName);
    }

    private String fetchTaskDefinitionName(Long rootDefinitionId) {
        if (rootDefinitionId != null) {
            TaskDefinition taskDefinition = taskDefinitionFacade.find(rootDefinitionId);
            return taskDefinition.getName();
        }

        return StringUtils.EMPTY;
    }

}
