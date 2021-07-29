package com.miotech.kun.workflow.operator.spark.clients;

import com.miotech.kun.commons.utils.ExceptionUtils;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public abstract class HttpApiClient {
    private static final Logger logger = LoggerFactory.getLogger(HttpApiClient.class);

    public static final String CONTENT_TYPE = "Content-Type";
    public static final MediaType JSON
            = MediaType.get("application/json; charset=utf-8");

    private OkHttpClient restClient = new OkHttpClient();

    public abstract String getBase();

    protected String post(String url, String message) {

        RequestBody body = RequestBody.create(JSON, message);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .addHeader(CONTENT_TYPE, JSON.toString())
                .addHeader("Accept", "application/json")
                .addHeader("Accept-charset", "UTF-8")
                .build();
        return request(request);
    }

    protected String put(String url, String message) {

        RequestBody body = RequestBody.create(JSON, message);
        Request request = new Request.Builder()
                .url(url)
                .put(body)
                .addHeader(CONTENT_TYPE, JSON.toString())
                .addHeader("Accept", "application/json")
                .addHeader("Accept-charset", "UTF-8")
                .build();
        return request(request);
    }

    protected String get(String url) {
        Request request = new Request.Builder().url(url)
                .addHeader(CONTENT_TYPE, JSON.toString())
                .build();
        return request(request);
    }

    protected String delete(String url) {
        Request request = new Request.Builder().url(url)
                .addHeader(CONTENT_TYPE, JSON.toString())
                .delete().build();
        return request(request);
    }

    protected String request(Request request) {
        if (logger.isDebugEnabled()) {
            logger.debug("Send http request to \"{}\" - \"{}\"", request.method(), request.url());
        }
        try (Response response = restClient.newCall(request).execute()) {
            int statusCode = response.code();
            if (statusCode < 200 || statusCode >= 400 ) {
                throw new IllegalArgumentException("Http API ERROR: " + response.body().string());
            }
            return response.body().string();
        } catch (IOException e) {
            throw ExceptionUtils.wrapIfChecked(e);
        }
    }

    protected String buildUrl(String api) {
        String baseUrl = this.getBase();
        if (!baseUrl.startsWith("http")) {
            baseUrl = "http://" + baseUrl;
        }
        return baseUrl + api;
    }

}
