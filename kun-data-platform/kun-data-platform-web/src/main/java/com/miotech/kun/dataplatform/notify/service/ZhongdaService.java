package com.miotech.kun.dataplatform.notify.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.miotech.kun.dataplatform.common.deploy.service.DeployedTaskService;
import com.miotech.kun.workflow.core.event.TaskAttemptStatusChangeEvent;
import com.miotech.kun.workflow.operator.spark.clients.HttpApiClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;
import java.util.Objects;

public class ZhongdaService extends HttpApiClient {
    /**
     * Zhongda 的 Host
     */
    @Value("")
    private String host;

    /**
     * Zhongda 的 Token 字段
     */
    private String token;

    /**
     * Zhongda 的 Group ID
     */
    private String group;

    @Autowired
    private DeployedTaskService deployedTaskService;

    public ZhongdaService(String host, String token, String group) {
        this.host = host;
        this.token = token;
        this.group = group;
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
        if (event.getToStatus().isFailure() && !Objects.equals(event.getTaskName(), "mse-task")){
            String msg = buildMessage(event);
            List<String> users = deployedTaskService.getUserByTaskId(event.getTaskId());
            doMessagePost(msg, users);
        }
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

    private String buildMessage(TaskAttemptStatusChangeEvent event){
        return String.format("Task: '%s' in state: %s", event.getTaskName(), event.getToStatus().name());
    }
}
