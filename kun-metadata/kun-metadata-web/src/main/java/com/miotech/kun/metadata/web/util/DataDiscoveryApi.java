package com.miotech.kun.metadata.web.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.Preconditions;
import com.miotech.kun.common.model.RequestResult;
import com.miotech.kun.metadata.web.model.vo.DataSourcePage;
import com.miotech.kun.workflow.client.WorkflowApiException;
import com.miotech.kun.workflow.utils.JSONUtils;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class DataDiscoveryApi {
    private final Logger logger = LoggerFactory.getLogger(DataDiscoveryApi.class);
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String PASS_TOKEN = "pass-token";
    private static final String PASS_TOKEN_VALUE = "***REMOVED***";
    private static final MediaType APPLICATION_JSON = MediaType.parse("application/json; charset=utf-8");
    private static final String DATASOURCE = "/kun/api/v1/metadata/datasources";
    private static final String DATASOURCE_TYPE = "/kun/api/v1/metadata/datasource/types";

    private final String baseUrl;
    private final OkHttpClient client;

    private final Long READ_TIME_OUT = 3 * 60 * 1000L;
    private final Long WRITE_TIME_OUT = 60 * 1000l;

    public DataDiscoveryApi(String url) {
        this.baseUrl = url;
        this.client = new OkHttpClient.Builder()
                .writeTimeout(WRITE_TIME_OUT, TimeUnit.MILLISECONDS)
                .readTimeout(READ_TIME_OUT, TimeUnit.MILLISECONDS)
                .build();
    }

    private HttpUrl getUrl(String path) {
        return buildUrl(path).build();
    }

    private HttpUrl.Builder buildUrl(String path) {
        HttpUrl url = HttpUrl.get(baseUrl + path);
        return url.newBuilder();
    }

    private String sendRequest(Request request) {
        Call call = client.newCall(request);
        try (Response response = call.execute()) {
            int statusCode = response.code();
            if (statusCode >= 400 || statusCode < 200) {
                throw new WorkflowApiException(String.format("\"%s %s\" is not valid: %s", request.method(), request.url(), response.body().string()));
            }
            return response.body().string();
        } catch (IOException e) {
            logger.error("Failed to execute request {}", request, e);
            throw new WorkflowApiException("Failed to get response to " + request.url(), e);
        }
    }

    private <T> T sendRequest(Request request, Class<T> clz) {
        return JSONUtils.jsonToObject(sendRequest(request), clz);
    }

    private <T> T sendRequest(Request request, TypeReference<T> typeRef) {
        return JSONUtils.jsonToObject(sendRequest(request), typeRef);
    }

    private <T> T post(HttpUrl url, Object payload, Class<T> responseClz) {
        Request request = new Request.Builder().url(url)
                .post(jsonBody(payload))
                .build();
        return sendRequest(request, responseClz);
    }

    private <T> T put(HttpUrl url, Object payload, Class<T> responseClz) {
        Request request = new Request.Builder().url(url)
                .put(jsonBody(payload))
                .build();
        return sendRequest(request, responseClz);
    }

    private <T> T get(HttpUrl url, Class<T> responseClz) {
        Request request = new Request.Builder().url(url)
                .get()
                .addHeader(CONTENT_TYPE, APPLICATION_JSON.toString())
                .build();
        return sendRequest(request, responseClz);
    }

    private void delete(HttpUrl url) {
        Request request = new Request.Builder().url(url)
                .delete()
                .addHeader(CONTENT_TYPE, APPLICATION_JSON.toString())
                .build();
        sendRequest(request, Object.class);
    }

    private RequestBody jsonBody(Object payload) {
        Preconditions.checkNotNull(payload, "request body should not be null");
        return RequestBody.create(APPLICATION_JSON, JSONUtils.toJsonString(payload));
    }

    public RequestResult<DataSourcePage> searchDataSources() {
        HttpUrl url = buildUrl(DATASOURCE).build();

        Request request = new Request.Builder().url(url)
                .get()
                .addHeader(CONTENT_TYPE, APPLICATION_JSON.toString())
                .addHeader(PASS_TOKEN, PASS_TOKEN_VALUE)
                .build();
        return sendRequest(request, new TypeReference<RequestResult<DataSourcePage>>() {});
    }

}
