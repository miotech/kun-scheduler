package com.miotech.kun.monitor.alert.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class WeComGetTokenResult extends WeComBaseResult {

    @JsonProperty("access_token")
    private String accessToken;

    private int expires_in;

}
