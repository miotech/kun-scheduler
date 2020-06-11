package com.miotech.kun.workflow.operator.model.clients;


import com.miotech.kun.workflow.operator.model.LivyApiException;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public abstract class HttpApiClient {
    private static final Logger logger = LoggerFactory.getLogger(HttpApiClient.class);

    public static final MediaType JSON
            = MediaType.get("application/json; charset=utf-8");

    private OkHttpClient restClient = new OkHttpClient();

    public abstract String getBase();

    protected String post(String url, String message) throws IOException {

        RequestBody body = RequestBody.create(JSON, message);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .addHeader("Accept", "application/json")
                .addHeader("Accept-charset", "UTF-8")
                .build();
        try (Response response = restClient.newCall(request).execute()) {
            if(response.code() != 201){
                logger.error("post method failed, error msg -> " + response.message() + ", url -> " + url + ", request -> " + message);
                throw new LivyApiException(response.message());
            }
            return response.body().string();
        }

    }

    protected String get(String url) throws IOException{
        Request request = new Request.Builder().url(url).build();
        try (Response response = restClient.newCall(request).execute()) {
            return response.body().string();
        }
    }

    protected String delete(String url) throws IOException{
        Request request = new Request.Builder().url(url).delete().build();
        try (Response response = restClient.newCall(request).execute()) {
            return response.body().string();
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
