package com.miotech.kun.monitor.alert.service;

import com.google.common.collect.ImmutableList;
import com.miotech.kun.commons.pubsub.event.Event;
import com.miotech.kun.dataplatform.facade.BackfillFacade;
import com.miotech.kun.dataplatform.facade.DeployedTaskFacade;
import com.miotech.kun.monitor.alert.config.NotifyLinkConfig;
import com.miotech.kun.security.model.UserInfo;
import com.miotech.kun.workflow.core.event.TaskAttemptStatusChangeEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class WeComService {

    @Value("${notify.wecom.chatid}")
    private String chatid;

    @Autowired
    private WeComSender weComSender;

    @Autowired
    private DeployedTaskFacade deployedTaskFacade;

    @Autowired
    private BackfillFacade backfillFacade;

    @Autowired
    private NotifyLinkConfig notifyLinkConfig;

    public void sendMessage(Event event) {
        if (event instanceof TaskAttemptStatusChangeEvent) {
            TaskAttemptStatusChangeEvent taskAttemptStatusChangeEvent = (TaskAttemptStatusChangeEvent) event;
            UserInfo userInfo = deployedTaskFacade.getUserByTaskId(taskAttemptStatusChangeEvent.getTaskId());
            String msg = buildMessage(taskAttemptStatusChangeEvent, userInfo);
            sendMessage(taskAttemptStatusChangeEvent.getTaskId(), msg);
        }
    }

    public void sendMessage(Long workflowTaskId, String msg) {
        weComSender.sendMessage(chatid, msg);
        UserInfo userInfo = deployedTaskFacade.getUserByTaskId(workflowTaskId);
        if (userInfo != null && StringUtils.isNotBlank(userInfo.getWeComId())) {
            weComSender.sendMessage(ImmutableList.of(userInfo.getWeComId()), msg);
        }
    }

    private String buildMessage(TaskAttemptStatusChangeEvent event, UserInfo userInfo) {
        if (notifyLinkConfig.isEnabled()) {
            long taskRunId = event.getTaskRunId();
            Optional<Long> derivingBackfillId = backfillFacade.findDerivedFromBackfill(taskRunId);
            Optional<Long> taskDefinitionId = deployedTaskFacade.findByWorkflowTaskId(event.getTaskId()).map(deployedTask -> deployedTask.getDefinitionId());
            log.debug("Pushing status message with link. task run id = {}, backfill id = {}, task definition id = {}.", taskRunId, derivingBackfillId.orElse(null), taskDefinitionId.orElse(null));
            // If it is not a backfill task run, and corresponding deployment task is found, then it should be a scheduled task run
            if (taskDefinitionId.isPresent() && (!derivingBackfillId.isPresent())) {
                return String.format("Deployed task: '%s' in state: %s%nTask owner : %s%nSee link: %s",
                        event.getTaskName(),
                        event.getToStatus().name(),
                        userInfo.getUsername(),
                        notifyLinkConfig.getScheduledTaskLinkURL(taskDefinitionId.get(), taskRunId)
                );
            }
        }
        return String.format("Task: '%s' in state: %s", event.getTaskName(), event.getToStatus().name());
    }

}
