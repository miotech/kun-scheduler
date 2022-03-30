package com.miotech.kun.monitor.alert.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WeComToGroupMessage extends WeComBaseMessage {

    private String chatid;

    public static WeComToGroupMessage from(String chatid, String content) {
        return from(chatid, "text", content);
    }

    public static WeComToGroupMessage from(String chatid, String msgType, String content) {
        WeComToGroupMessage weComToUserMessage = new WeComToGroupMessage();
        weComToUserMessage.setMsgtype(msgType);
        weComToUserMessage.setText(new Text(content));
        weComToUserMessage.setChatid(chatid);

        return weComToUserMessage;
    }

}
