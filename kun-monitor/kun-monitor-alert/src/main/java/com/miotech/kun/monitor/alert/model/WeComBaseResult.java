package com.miotech.kun.monitor.alert.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class WeComBaseResult {

    private int errcode;

    private String errmsg;

    public boolean isSuccess() {
        return errcode == 0;
    }

}
