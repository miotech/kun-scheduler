package com.miotech.kun.workflow.operator.spark.clients;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.miotech.kun.workflow.operator.spark.LivyApiException;
import com.miotech.kun.workflow.operator.spark.models.*;
import com.miotech.kun.workflow.utils.JSONUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LivyClient extends HttpApiClient {
    private static final Logger logger = LoggerFactory.getLogger(LivyClient.class);
    private static final String SESSIONS = "/sessions/";
    private static final String BATCHES = "/batches/";

    private String base;
    private String queue;
    private String proxyUser;
    private ObjectMapper objectMapper = new ObjectMapper()
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
        if (Strings.isNullOrEmpty(job.getQueue())
                    && !Strings.isNullOrEmpty(queue)) {
                job.setQueue(queue);
        }
        if (!Strings.isNullOrEmpty(proxyUser)) {
                job.setProxyUser(proxyUser);
        }

        String payload = JSONUtils.toJsonString(job);
        String response = createBatch(payload);
        return toObject(response, SparkApp.class);
    }

    public SparkApp runSparkSession(SparkJob job) {
        if (Strings.isNullOrEmpty(job.getQueue())
                && !Strings.isNullOrEmpty(queue)) {
            job.setQueue(queue);
        }
        if (!Strings.isNullOrEmpty(proxyUser)) {
            job.setProxyUser(proxyUser);
        }

        String payload = JSONUtils.toJsonString(job);
        String response = createSession(payload);
        return toObject(response, SparkApp.class);
    }

    public LivyStateInfo getSparkSessionState(Integer sessionId) {
        String url = buildUrl(SESSIONS + sessionId + "/state");
        return toObject(this.get(url), LivyStateInfo.class);
    }

    public Statement runSparkSQL(Integer sessionId, String sql) {
        StatementRequest request = new StatementRequest();
        request.setCode(sql);
        request.setKind("sql");
        String response = createSessionStatement(sessionId, request);
        return toObject(response, Statement.class);
    }

    public Statement getStatement(Integer sessionId, Integer statementId) {
        String url = buildUrl(SESSIONS + sessionId + "/statements/" + statementId);
        return toObject(this.get(url), Statement.class);
    }

    public SparkApp getSparkSession(Integer sparkSessionID) {
        return toObject(getSession(sparkSessionID), SparkApp.class);
    }

    public SparkApp getSparkJob(Integer sparkJobID) {
        return toObject(getBatch(sparkJobID), SparkApp.class);
    }

    public LivyStateInfo getSparkJobState(Integer sparkJobID) {
        String url = buildUrl(BATCHES + sparkJobID + "/state");
        return toObject(this.get(url), LivyStateInfo.class);
    }

    public String createBatch(String jobPayload){
        String url = buildUrl("/batches");
        return this.post(url, jobPayload);
    }

    public String getBatch(Integer batchId) {
        String url = buildUrl(BATCHES + batchId);
        return this.get(url);
    }

    public String deleteBatch(Integer batchId) {
        String url = buildUrl(BATCHES + batchId);
        return this.delete(url);
    }

    public String createSession(String jobPayload) {
        String url = buildUrl("/sessions");
        return this.post(url, jobPayload);
    }

    public String getSession(Integer sessionId) {
        String url = buildUrl(SESSIONS + sessionId);
        return this.get(url);
    }

    public String deleteSession(Integer sessionId) {
        String url = buildUrl(SESSIONS + sessionId);
        return this.delete(url);
    }

    public String createSessionStatement(Integer sessionId, StatementRequest statementRequest) {
        String url = buildUrl(SESSIONS + sessionId + "/statements");
        String payload = JSONUtils.toJsonString(statementRequest);
        return this.post(url, payload);
    }

    public String cancelSessionStatement(Integer sessionId, Integer statementId) {
        String url = buildUrl(SESSIONS + sessionId + "/statements/" + statementId + "/cancel");
        return this.post(url, "{}");
    }

    public String getSessionLog(Integer sessionId) {
        String url = buildUrl(SESSIONS + sessionId + "/log");
        return this.get(url);
    }

    private <T> T toObject(String content, Class<T> valueType) {
        try {
            return objectMapper.readValue(content, valueType);
        } catch (JsonProcessingException e) {
            logger.error("Failed to parse \"{}\" to class \"{}\"", content, valueType.getName(), e);
            throw new LivyApiException(e.getMessage());
        }
    }

}
