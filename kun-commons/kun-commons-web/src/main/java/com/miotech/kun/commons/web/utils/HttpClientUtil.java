package com.miotech.kun.commons.web.utils;

import com.google.inject.Singleton;
import com.miotech.kun.commons.utils.ExceptionUtils;
import org.apache.http.Consts;
import org.apache.http.client.config.RequestConfig;
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
        httpGet.setConfig(RequestConfig.custom().setSocketTimeout(10000).setConnectTimeout(10000).build());
        String result;
        try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
            result = EntityUtils.toString(response.getEntity(), Consts.UTF_8);
        } catch (Exception e) {
            logger.error("HttpClientUtil.doGet url={} error:", url, e);
            throw ExceptionUtils.wrapIfChecked(e);
        }

        return result;
    }

    public String doPost(String url, String json) {
        HttpPost httpPost = new HttpPost(url);
        httpPost.setConfig(RequestConfig.custom().setSocketTimeout(10000).setConnectTimeout(10000).build());
        StringEntity entity = new StringEntity(json, ContentType.APPLICATION_JSON);
        httpPost.setEntity(entity);
        String result;
        try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
            result = EntityUtils.toString(response.getEntity(), Consts.UTF_8);
        } catch (Exception e) {
            logger.error("HttpClientUtil.doPost url={} json={} error:", url, json, e);
            throw ExceptionUtils.wrapIfChecked(e);
        }

        return result;
    }

}
