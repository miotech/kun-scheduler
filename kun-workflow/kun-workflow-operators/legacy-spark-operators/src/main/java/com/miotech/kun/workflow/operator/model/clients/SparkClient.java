package com.miotech.kun.workflow.operator.model.clients;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.miotech.kun.workflow.operator.model.models.Application;

import java.io.IOException;

public class SparkClient extends HttpApiClient {
    private String host;
    private Integer port;
    private ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public SparkClient(String host, Integer port) {
        this.host = host;
        this.port = port;
    }

    @Override
    public String getBase() {
        return String.format("%s:%s", host, port);
    }

    public Application getApp(String appId) {
        try {
            return objectMapper.readValue(getApplication(appId), Application.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }catch (IOException e){
            throw new RuntimeException(e);
        }
    }

    public String getApplication(String applicationId) throws IOException {
        String appUrl = buildUrl(String.format("/ws/v1/cluster/apps/%s", applicationId));
        return get(appUrl);
    }

}
