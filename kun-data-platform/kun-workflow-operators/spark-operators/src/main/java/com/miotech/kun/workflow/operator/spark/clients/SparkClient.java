package com.miotech.kun.workflow.operator.spark.clients;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.miotech.kun.commons.utils.ExceptionUtils;
import com.miotech.kun.workflow.operator.spark.models.Application;

import java.io.IOException;

public class SparkClient extends HttpApiClient {
    private String address;
    private ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public SparkClient(String address) {
        this.address = address;
    }

    @Override
    public String getBase() {
        return address;
    }

    public Application getApp(String appId) {
        try {
            return objectMapper.readValue(getApplication(appId), Application.class);
        } catch (IOException e) {
            throw ExceptionUtils.wrapIfChecked(e);
        }
    }

    private String getApplication(String applicationId) {
        String appUrl = buildUrl(String.format("/ws/v1/cluster/apps/%s", applicationId));
        return get(appUrl);
    }

    public void killApplication(String appId){
        String url = buildUrl(String.format("/ws/v1/cluster/apps/%s/state", appId));
        put(url, "{\"state\": \"KILLED\"}");
    }

}
