package com.miotech.kun.workflow.operator.model.clients;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.miotech.kun.workflow.operator.model.LivyApiException;
import com.miotech.kun.workflow.operator.model.models.SparkApp;
import com.miotech.kun.workflow.operator.model.models.SparkJob;
import com.miotech.kun.workflow.utils.JSONUtils;

import java.io.IOException;

public class LivyClient extends HttpApiClient {
    private String base;
    private String queue;
    private String proxyUser;
    ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .setSerializationInclusion(JsonInclude.Include.NON_ABSENT)
            .setSerializationInclusion(JsonInclude.Include.NON_EMPTY);

    public LivyClient(String host) {
        this(host, null, null);
    }

    public LivyClient(String host, String queue, String proxyUser) {
        base = host;
        this.proxyUser = proxyUser;
        this.queue = queue;
    }

    @Override
    public String getBase() {
        return base;
    }

    public SparkApp runSparkJob(SparkJob job) {
        try {
            if (Strings.isNullOrEmpty(job.getQueue())
                    && !Strings.isNullOrEmpty(queue)) {
                job.setQueue(queue);
            }
            if (!Strings.isNullOrEmpty(proxyUser)) {
                job.setProxyUser(proxyUser);
            }

            String payload = JSONUtils.toJsonString(job);
            String response = createBatch(payload);
            return objectMapper.readValue(response, SparkApp.class);

        }catch (IOException e){
            throw new RuntimeException(e);
        }
    }



    public SparkApp getSparkJob(int sparkJobID) {
        try {
            return objectMapper.readValue(getBatch(sparkJobID), SparkApp.class);
        } catch (IOException e) {
            throw new LivyApiException("");
        }
    }


    public String createBatch(String jobPayload){
        String url = buildUrl("/batches");
        try {
            return this.post(url, jobPayload);
        } catch (IOException e) {
            throw new LivyApiException(e.getMessage());
        }
    }

    public String getBatch(Integer batchId) {
        String url = buildUrl("/batches/" + batchId);
        try {
            return this.get(url);
        } catch (IOException e) {
            throw new LivyApiException(e.getMessage());
        }
    }

    public String getBatchState(Integer batchId) {
        String url = buildUrl("/batches/" + batchId + "/state");
        try {
            return this.get(url);
        } catch (IOException e) {
            throw new LivyApiException(e.getMessage());
        }
    }
    public String deleteBatch(Integer batchId) {
        String url = buildUrl("/batches/" + batchId);
        try {
            return this.delete(url);
        } catch (IOException e) {
            throw new LivyApiException(e.getMessage());
        }
    }

//    public String getBatchLog(Integer batchId, Integer from) {
//        String url = buildUrl("/batches/" + batchId + "/log?from=" + from);
//        return this.getBody(this.get(url));
//    }
//
//    public LogInfo getSparkJoblog(int sparkJobID, int from) {
//        try {
//            return (LogInfo) JSONUtils.toMessage(getBatchLog(sparkJobID, from), LogInfo.newBuilder());
//        } catch (InvalidProtocolBufferException e) {
//            throw new LivyApiException("");
//        }
//    }

}
