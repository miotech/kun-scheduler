package com.miotech.kun.monitor.alert.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.miotech.kun.commons.utils.HttpApiClient;
import com.miotech.kun.dataplatform.facade.BackfillFacade;
import com.miotech.kun.dataplatform.facade.DeployedTaskFacade;
import com.miotech.kun.monitor.alert.config.NotifyLinkConfig;
import com.miotech.kun.workflow.core.event.TaskAttemptStatusChangeEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

@Slf4j
public class ZhongdaService extends HttpApiClient {
    /**
     * Zhongda 的 Host
     */
    private String host;

    /**
     * Zhongda 的 Token 字段
     */
    private String token;

    /**
     * Zhongda 的 Group ID
     */
    private String group;

    /**
     * 通知中的链接相关配置上下文
     */
    private NotifyLinkConfig notifyLinkConfig;

    @Autowired
    private DeployedTaskFacade deployedTaskFacade;

    @Autowired
    private BackfillFacade backfillFacade;

    public ZhongdaService(String host, String token, String group, NotifyLinkConfig notifyLinkConfig) {
        this.host = host;
        this.token = token;
        this.group = group;
        this.notifyLinkConfig = notifyLinkConfig;
    }

    @Override
    public String getBase() {
        return host;
    }

    public void sendMessage(Long workflowTaskId, String msg) {
        List<String> users = deployedTaskFacade.getUserByTaskId(workflowTaskId);
        doMessagePost(msg, users);
    }

    /**
     * Send a message by a task attempt status change event
     * @param event status change event object
     */
    public void sendMessage(TaskAttemptStatusChangeEvent event) {
        List<String> users = deployedTaskFacade.getUserByTaskId(event.getTaskId());
        String msg = buildMessage(event, users);
        sendMessage(event.getTaskId(), msg);
    }

    private void doMessagePost(String content, List<String> users) {
        String api = buildUrl("/alertservice/send-wechat");

        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("token", token);
        payload.put("body", content);

        //send message to group
        payload.put("group", group);
        post(api, payload.toString());

        //send message to owner
        payload.remove("group");
        payload.put("user_list", String.join(",", users));
        post(api, payload.toString());
    }

    private String buildMessage(TaskAttemptStatusChangeEvent event, List<String> users) {
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
                        String.join(",", users),
                        notifyLinkConfig.getScheduledTaskLinkURL(taskDefinitionId.get(), taskRunId)
                );
            }
            // TODO: @joshoy generate a link for backfill webpage. Should be supported by frontend UI first.
        }
        return String.format("Task: '%s' in state: %s", event.getTaskName(), event.getToStatus().name());
    }
}
