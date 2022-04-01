package com.miotech.kun.monitor.alert.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WeComToChatMessage extends WeComBaseMessage {

    private String chatid;

    public static WeComToChatMessage from(String chatid, String content) {
        return from(chatid, "text", content);
    }

    public static WeComToChatMessage from(String chatid, String msgType, String content) {
        WeComToChatMessage weComToChatMessage = new WeComToChatMessage();
        weComToChatMessage.setMsgtype(msgType);
        weComToChatMessage.setText(new Text(content));
        weComToChatMessage.setChatid(chatid);

        return weComToChatMessage;
    }

}
