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

    private static final String MSG_TEMPLATE = "[reason]: %s%n[task]: %s%n[result]: %s%n[owner]: %s%n[link]: %s";

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

    public void sendMessage(List<String> weComUserIds, String msg) {
        weComSender.sendMessageToUsers(weComUserIds, msg);
    }


    public void sendMessage(Long workflowTaskId, String msg) {
        try {
            weComSender.sendMessageToChat(chatid, msg);
        } catch (Exception e) {
            log.error("an exception occurred during send message: {} to wecom chat: {}.", msg, chatid);
            log.error("msg: {}", e.getMessage());
        }

        try {
            UserInfo userInfo = deployedTaskFacade.getUserByTaskId(workflowTaskId);
            if (userInfo != null && StringUtils.isNotBlank(userInfo.getWeComId())) {
                weComSender.sendMessageToUsers(ImmutableList.of(userInfo.getWeComId()), msg);
            }
        } catch (Exception e) {
            log.error("an exception occurred during send message: {} to wecom user.", msg);
            log.error("msg: {}", e.getMessage());
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
                String reason = event.getToStatus().isSuccess() ? "Notification" : "Failure";
                String result = event.getToStatus().isSuccess() ? "task succeeded" : "task failed";
                return String.format(MSG_TEMPLATE,
                        reason,
                        event.getTaskName(),
                        result,
                        userInfo.getUsername(),
                        notifyLinkConfig.getScheduledTaskLinkURL(taskDefinitionId.get(), taskRunId)
                );
            }
        }
        return String.format("Task: '%s' in state: %s", event.getTaskName(), event.getToStatus().name());
    }

}
