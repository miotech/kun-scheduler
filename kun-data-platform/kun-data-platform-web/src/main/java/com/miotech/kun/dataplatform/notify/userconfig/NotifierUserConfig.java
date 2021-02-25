package com.miotech.kun.dataplatform.notify.userconfig;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "notifierType")
@JsonSubTypes({
        @JsonSubTypes.Type(name = "ZHONGDA", value = ZhongdaNotifierUserConfig.class),
        @JsonSubTypes.Type(name = "EMAIL", value = EmailNotifierUserConfig.class)
})
@JsonSerialize
public abstract class NotifierUserConfig {
    @JsonProperty(value = "notifierType")
    private final String notifierType;

    public String getNotifierType() {
        return this.notifierType;
    }

    NotifierUserConfig(String notifierType) {
        this.notifierType = notifierType;
    }
}
