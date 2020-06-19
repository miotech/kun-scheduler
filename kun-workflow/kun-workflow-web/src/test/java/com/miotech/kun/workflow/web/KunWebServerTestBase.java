package com.miotech.kun.workflow.web;

import com.google.inject.Inject;
import com.miotech.kun.commons.testing.GuiceTestBase;
import com.miotech.kun.commons.utils.ExceptionUtils;
import com.miotech.kun.workflow.SchedulerModule;
import com.miotech.kun.workflow.common.constant.ConfigurationKeys;
import com.miotech.kun.workflow.db.DatabaseModule;
import com.miotech.kun.workflow.utils.PropertyUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.junit.After;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Properties;

public class KunWebServerTestBase extends GuiceTestBase {

    private final Logger logger = LoggerFactory.getLogger(KunWebServerTestBase.class);

    @Inject
    private KunWebServer webServer;

    @Inject
    private Properties props;

    @Override
    protected void configuration() {
        super.configuration();
        Properties props = PropertyUtils.loadAppProps("application-test.yaml");
        addModules(new KunWebServerModule(props), new DatabaseModule(), new SchedulerModule());
    }

    @Before
    public void setUp() {
        new Thread(() -> webServer.start()).start();
        while(!webServer.isServerRunning()) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                throw ExceptionUtils.wrapIfChecked(e);
            }
        }
    }

    @After
    public void tearDown() {
        webServer.shutdown();
    }

    protected String post(String url, String message){
        HttpPost postRequest = new HttpPost(buildUrl(url));
        try {
            postRequest.setEntity(new StringEntity(message));
            return request(postRequest);
        } catch (UnsupportedEncodingException e) {
            throw ExceptionUtils.wrapIfChecked(e);
        }
    }

    protected String put(String url, String message){
        HttpPut putRequest = new HttpPut(buildUrl(url));
        try {
            putRequest.setEntity(new StringEntity(message));
            return request(putRequest);
        } catch (UnsupportedEncodingException e) {
            throw ExceptionUtils.wrapIfChecked(e);
        }
    }

    protected String get(String url) {
        return request(new HttpGet(buildUrl(url)));
    }

    protected String delete(String url) {
        return request(new HttpDelete(buildUrl(url)));
    }

    private String buildUrl(String url) {
        String port = props.getProperty(ConfigurationKeys.PROP_SERVER_PORT, "8088");
        if (!url.startsWith("/")) {
            url = "/" + url;
        }
        return "http://localhost:" + port + url;
    }

    private String request(HttpUriRequest request) {
        try {
            HttpResponse responseEntity = HttpClientBuilder.create().build().execute(request);
            String response = EntityUtils.toString(responseEntity.getEntity());
            int statusCode = responseEntity.getStatusLine().getStatusCode();

            if ( statusCode < 200 ||
                    statusCode >= 400 ) {
                logger.error("Http ERROR: " +  response);
            }
            return response;
        } catch (IOException e) {
            logger.error("", e);
            throw ExceptionUtils.wrapIfChecked(e);
        }
    }
}
