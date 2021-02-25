package com.miotech.kun.dataplatform.notify.userconfig;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

import java.util.List;

@JsonTypeName("EMAIL")
public class EmailNotifierUserConfig extends NotifierUserConfig {
    private final List<String> emailList;

    private final List<Long> userIdList;

    public List<String> getEmailList() {
        return this.emailList;
    }

    public List<Long> getUserIdList() {
        return this.userIdList;
    }

    @JsonCreator
    public EmailNotifierUserConfig(
            @JsonProperty("emailList") List<String> emailList,
            @JsonProperty("userIdList") List<Long> userIdList
    ) {
        super("EMAIL");
        this.emailList = emailList;
        this.userIdList = userIdList;
    }
}
