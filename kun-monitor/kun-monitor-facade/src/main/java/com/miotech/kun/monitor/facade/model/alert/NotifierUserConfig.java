package com.miotech.kun.monitor.facade.model.alert;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.miotech.kun.monitor.facade.constant.NotifierTypeNameConstants;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "notifierType")
@JsonSubTypes({
        @JsonSubTypes.Type(name = NotifierTypeNameConstants.WECOM, value = WeComNotifierUserConfig.class),
        @JsonSubTypes.Type(name = NotifierTypeNameConstants.EMAIL, value = EmailNotifierUserConfig.class)
})
@JsonSerialize
public abstract class NotifierUserConfig {
    @JsonProperty(value = "notifierType")
    private final String notifierType;

    public String getNotifierType() {
        return this.notifierType;
    }

    public NotifierUserConfig(String notifierType) {
        this.notifierType = notifierType;
    }
}
