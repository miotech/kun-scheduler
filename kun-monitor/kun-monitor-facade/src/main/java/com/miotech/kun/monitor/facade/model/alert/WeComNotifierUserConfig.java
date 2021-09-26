package com.miotech.kun.monitor.facade.model.alert;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.miotech.kun.monitor.facade.constant.NotifierTypeNameConstants;

@JsonTypeName(NotifierTypeNameConstants.WECOM)
public class WeComNotifierUserConfig extends NotifierUserConfig {
    public WeComNotifierUserConfig() {
        super(NotifierTypeNameConstants.WECOM);
    }
}
