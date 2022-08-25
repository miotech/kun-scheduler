package com.miotech.kun.monitor.alert.model;

import lombok.Data;

@Data
public class WeComBaseMessage {

    private String msgtype;

    private Text markdown;

    public static class Text {

        private String content;

        public Text(String content) {
            this.content = content;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }
}
