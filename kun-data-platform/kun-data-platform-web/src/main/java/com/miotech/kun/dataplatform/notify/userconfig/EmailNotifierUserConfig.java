package com.miotech.kun.dataplatform.notify.userconfig;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.miotech.kun.dataplatform.constant.NotifierTypeNameConstants;

import java.util.List;
import java.util.Objects;

@JsonTypeName(NotifierTypeNameConstants.EMAIL)
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
        super(NotifierTypeNameConstants.EMAIL);
        this.emailList = emailList;
        this.userIdList = userIdList;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EmailNotifierUserConfig that = (EmailNotifierUserConfig) o;
        return Objects.equals(emailList, that.emailList) && Objects.equals(userIdList, that.userIdList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(emailList, userIdList);
    }
}
