package com.miotech.kun.commons.web.utils;

import com.miotech.kun.commons.utils.ExceptionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.joor.Reflect;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import java.io.IOException;
import java.net.SocketTimeoutException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class HttpClientUtilTest {

    private HttpClientUtil httpClientUtil = new HttpClientUtil();

    @Test
    public void testDoGet_ok() {
        CloseableHttpClient mockHttpClient = Mockito.mock(CloseableHttpClient.class);
        Reflect.on(httpClientUtil).set("httpClient", mockHttpClient);

        String response = "ok";
        try {
            CloseableHttpResponse mockHttpResponse = Mockito.mock(CloseableHttpResponse.class);
            Mockito.when(mockHttpResponse.getEntity()).thenReturn(new StringEntity(response));

            Mockito.when(mockHttpClient.execute(ArgumentMatchers.isA(HttpGet.class))).thenReturn(mockHttpResponse);
        } catch (IOException e) {
            throw ExceptionUtils.wrapIfChecked(e);
        }

        String result = httpClientUtil.doGet(StringUtils.EMPTY);
        assertThat(result, is(response));
    }

    @Test
    public void testDoGet_exception() {
        CloseableHttpClient mockHttpClient = Mockito.mock(CloseableHttpClient.class);
        Reflect.on(httpClientUtil).set("httpClient", mockHttpClient);

        try {
            Mockito.when(mockHttpClient.execute(ArgumentMatchers.isA(HttpGet.class))).thenThrow(new SocketTimeoutException("Read timed out"));
        } catch (IOException e) {
            throw ExceptionUtils.wrapIfChecked(e);
        }

        Throwable t = null;
        try {
            httpClientUtil.doGet(StringUtils.EMPTY);
        } catch (Exception e) {
            t = e;
        }

        assertThat(t.getCause().getMessage(), is("Read timed out"));
    }

    @Test
    public void testDoPost_ok() {
        CloseableHttpClient mockHttpClient = Mockito.mock(CloseableHttpClient.class);
        Reflect.on(httpClientUtil).set("httpClient", mockHttpClient);

        String response = "ok";
        try {
            CloseableHttpResponse mockHttpResponse = Mockito.mock(CloseableHttpResponse.class);
            Mockito.when(mockHttpResponse.getEntity()).thenReturn(new StringEntity(response));

            Mockito.when(mockHttpClient.execute(ArgumentMatchers.isA(HttpPost.class))).thenReturn(mockHttpResponse);
        } catch (IOException e) {
            throw ExceptionUtils.wrapIfChecked(e);
        }

        String result = httpClientUtil.doPost(StringUtils.EMPTY, StringUtils.EMPTY);
        assertThat(result, is(response));
    }

    @Test
    public void testDoPost_exception() {
        CloseableHttpClient mockHttpClient = Mockito.mock(CloseableHttpClient.class);
        Reflect.on(httpClientUtil).set("httpClient", mockHttpClient);

        try {
            Mockito.when(mockHttpClient.execute(ArgumentMatchers.isA(HttpPost.class))).thenThrow(new SocketTimeoutException("Read timed out"));
        } catch (Exception e) {
            throw ExceptionUtils.wrapIfChecked(e);
        }

        Throwable t = null;
        try {
            httpClientUtil.doPost(StringUtils.EMPTY, StringUtils.EMPTY);
        } catch (Exception e) {
            t = e;
        }

        assertThat(t.getCause().getMessage(), is("Read timed out"));
    }

}
