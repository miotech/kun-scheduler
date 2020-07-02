package com.miotech.kun.commons.web.utils;

import com.google.inject.Singleton;
import org.apache.http.Consts;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class HttpClientUtil {

    private static final Logger logger = LoggerFactory.getLogger(HttpClientUtil.class);
    private CloseableHttpClient httpClient = HttpClients.createDefault();

    public String doGet(String url) {
        HttpGet httpGet = new HttpGet(url);
        String result = "";
        try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
            result = EntityUtils.toString(response.getEntity(), Consts.UTF_8);
        } catch (Exception e) {
            logger.error("HttpClientUtil.doGet error:", e);
        }

        return result;
    }

    public String doPost(String url, String json) {
        HttpPost httpPost = new HttpPost(url);
        StringEntity entity = new StringEntity(json, ContentType.APPLICATION_JSON);
        httpPost.setEntity(entity);
        String result = "";
        try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
            result = EntityUtils.toString(response.getEntity(), Consts.UTF_8);
        } catch (Exception e) {
            logger.error("HttpClientUtil.doGet error:", e);
        }

        return result;
    }

}
