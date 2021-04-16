package com.miotech.kun.dataplatform.notify.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.miotech.kun.dataplatform.common.backfill.service.BackfillService;
import com.miotech.kun.dataplatform.common.deploy.service.DeployedTaskService;
import com.miotech.kun.dataplatform.config.NotifyLinkConfig;
import com.miotech.kun.workflow.core.event.TaskAttemptStatusChangeEvent;
import com.miotech.kun.workflow.operator.spark.clients.HttpApiClient;
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
    private DeployedTaskService deployedTaskService;

    @Autowired
    private BackfillService backfillService;

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

    /**
     * Send a message by a task attempt status change event
     * @param event status change event object
     */
    public void sendMessage(TaskAttemptStatusChangeEvent event) {
        String msg = buildMessage(event);
        List<String> users = deployedTaskService.getUserByTaskId(event.getTaskId());
        doMessagePost(msg, users);
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

    private String buildMessage(TaskAttemptStatusChangeEvent event) {
        if (notifyLinkConfig.isEnabled()) {
            long taskRunId = event.getTaskRunId();
            Optional<Long> derivingBackfillId = this.backfillService.findDerivedFromBackfill(taskRunId);
            Optional<Long> taskDefinitionId = deployedTaskService.findByWorkflowTaskId(event.getTaskId()).map(deployedTask -> deployedTask.getDefinitionId());
            log.debug("Pushing status message with link. task run id = {}, backfill id = {}, task definition id = {}.", taskRunId, derivingBackfillId.orElse(null), taskDefinitionId.orElse(null));
            // If it is not a backfill task run, and corresponding deployment task is found, then it should be a scheduled task run
            if (taskDefinitionId.isPresent() && (!derivingBackfillId.isPresent())) {
                return String.format("Deployed task: '%s' in state: %s%n%nSee link: %s",
                        event.getTaskName(),
                        event.getToStatus().name(),
                        notifyLinkConfig.getScheduledTaskLinkURL(taskDefinitionId.get(), taskRunId)
                );
            }
            // TODO: @joshoy generate a link for backfill webpage. Should be supported by frontend UI first.
        }
        return String.format("Task: '%s' in state: %s", event.getTaskName(), event.getToStatus().name());
    }
}
