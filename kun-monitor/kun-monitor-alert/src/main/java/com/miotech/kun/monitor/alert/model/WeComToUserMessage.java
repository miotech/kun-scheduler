package com.miotech.kun.monitor.alert.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WeComToUserMessage extends WeComBaseMessage {

    private String touser;

    private int agentid;

    public static WeComToUserMessage from(String toUser, int agentid, String content) {
        return from(toUser, agentid, "text", content);
    }

    public static WeComToUserMessage from(String toUser, int agentid, String msgType, String content) {
        WeComToUserMessage weComToUserMessage = new WeComToUserMessage();
        weComToUserMessage.setMsgtype(msgType);
        weComToUserMessage.setText(new Text(content));
        weComToUserMessage.setTouser(toUser);
        weComToUserMessage.setAgentid(agentid);

        return weComToUserMessage;
    }

}
