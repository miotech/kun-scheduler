package com.miotech.kun.dataplatform.notify;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.miotech.kun.dataplatform.common.deploy.service.DeployedTaskService;
import com.miotech.kun.workflow.core.event.Event;
import com.miotech.kun.workflow.core.event.TaskAttemptStatusChangeEvent;
import com.miotech.kun.workflow.operator.spark.clients.HttpApiClient;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;


public class ZhongdaNotifier extends HttpApiClient implements MessageNotifier {
    private String host;
    private String token;

    @Autowired
    private DeployedTaskService deployedTaskService;


    public ZhongdaNotifier(String host, String token) {
        this.host = host;
        this.token = token;
    }

    @Override
    public String getBase() {
        return host;
    }

    public String sendMessage(String content, String group, List<String> users) {
        String api = buildUrl("/alertservice/send-wechat");

        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("token", token);
        payload.put("group", group);
        payload.put("body", content);

        payload.put("user_list", String.join(",", users));
        return post(api, payload.toString());
    }

    @Override
    public void notify(Event event) {
        TaskAttemptStatusChangeEvent taskAttemptStatusChangeEvent = (TaskAttemptStatusChangeEvent) event;
        if(taskAttemptStatusChangeEvent.getToStatus().isFailure() || taskAttemptStatusChangeEvent.getToStatus().isSuccess()){
            String msg = buildMessage(taskAttemptStatusChangeEvent);
            List<String> users = deployedTaskService.getUserByTaskId(taskAttemptStatusChangeEvent.getTaskId());
            sendMessage(msg, "", users);
        }
    }

    public String buildMessage(TaskAttemptStatusChangeEvent event){
        return String.format("Task: '%s' in state: %s", event.getTaskName(), event.getToStatus().name());
    }

}
