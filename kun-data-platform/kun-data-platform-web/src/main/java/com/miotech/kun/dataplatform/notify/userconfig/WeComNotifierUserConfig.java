package com.miotech.kun.dataplatform.notify.userconfig;

import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("WECOM")
public class WeComNotifierUserConfig extends NotifierUserConfig {
    public WeComNotifierUserConfig() {
        super("WECOM");
    }
}
