package com.miotech.kun.dataplatform.notify.userconfig;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.miotech.kun.dataplatform.constant.NotifierTypeNameConstants;

@JsonTypeName(NotifierTypeNameConstants.WECOM)
public class WeComNotifierUserConfig extends NotifierUserConfig {
    public WeComNotifierUserConfig() {
        super(NotifierTypeNameConstants.WECOM);
    }
}
