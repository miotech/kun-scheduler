package com.miotech.kun.commons.web.utils;

import com.google.inject.Singleton;
import com.miotech.kun.commons.utils.ExceptionUtils;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

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

    public String uploadFile(String url, File file){
        MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();
        FileBody bin = new FileBody(file);
        HttpEntity entity = multipartEntityBuilder.addPart("file",bin).build();
        HttpPost httpPost = new HttpPost(url);
        httpPost.setEntity(entity);
        String result;
        try {
            CloseableHttpResponse res = httpClient.execute(httpPost);
            result = EntityUtils.toString(res.getEntity(), Consts.UTF_8);
        }catch (Exception e){
            logger.error("HttpClientUtil.uploadFile url={} json={} error:", url, file.getName(), e);
            throw ExceptionUtils.wrapIfChecked(e);
        }
        return result;
    }

    public String doDelete(String url){
        HttpDelete httpDelete = new HttpDelete(url);
        String result;
        try {
            CloseableHttpResponse res = httpClient.execute(httpDelete);
            result = EntityUtils.toString(res.getEntity(), Consts.UTF_8);
        }catch (Exception e){
            logger.error("HttpClientUtil.doDelete url={}",url,e);
            throw ExceptionUtils.wrapIfChecked(e);
        }
        return result;
    }

}
