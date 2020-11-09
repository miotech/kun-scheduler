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
    private String group;

    @Autowired
    private DeployedTaskService deployedTaskService;


    public ZhongdaNotifier(String host, String token, String group) {
        this.host = host;
        this.token = token;
        this.group = group;
    }

    @Override
    public String getBase() {
        return host;
    }

    public void sendMessage(String content, List<String> users) {
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

    @Override
    public void notify(Event event) {
        TaskAttemptStatusChangeEvent taskAttemptStatusChangeEvent = (TaskAttemptStatusChangeEvent) event;
        if(taskAttemptStatusChangeEvent.getToStatus().isFailure() || taskAttemptStatusChangeEvent.getToStatus().isSuccess()){
            String msg = buildMessage(taskAttemptStatusChangeEvent);
            List<String> users = deployedTaskService.getUserByTaskId(taskAttemptStatusChangeEvent.getTaskId());
            sendMessage(msg, users);
        }
    }

    public String buildMessage(TaskAttemptStatusChangeEvent event){
        return String.format("Task: '%s' in state: %s", event.getTaskName(), event.getToStatus().name());
    }

}
