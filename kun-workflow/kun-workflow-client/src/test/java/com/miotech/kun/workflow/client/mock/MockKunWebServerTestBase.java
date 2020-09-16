package com.miotech.kun.workflow.client.mock;

import com.google.inject.Inject;
import com.miotech.kun.commons.db.DatabaseModule;
import com.miotech.kun.commons.testing.GuiceTestBase;
import com.miotech.kun.commons.utils.Props;
import com.miotech.kun.commons.utils.PropsUtils;
import com.miotech.kun.workflow.SchedulerModule;
import com.miotech.kun.workflow.common.constant.ConfigurationKeys;
import com.miotech.kun.workflow.web.KunWorkflowServerModule;
import com.miotech.kun.workflow.web.KunWorkflowWebServer;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.junit.After;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;

public class MockKunWebServerTestBase extends GuiceTestBase {

    private final Logger logger = LoggerFactory.getLogger(MockKunWebServerTestBase.class);

    private final OkHttpClient okHttpClient = new OkHttpClient();

    @Inject
    private KunWorkflowWebServer webServer;

    @Inject
    private Props props;

    @Override
    protected void configuration() {
        super.configuration();
        Props props = PropsUtils.loadAppProps("application-test.yaml");
        int port = 18080 + (new Random()).nextInt(100);
        logger.info("Start test workflow server in : localhost:{}", port);
        props.put(ConfigurationKeys.PROP_SERVER_PORT, Integer.toString(port));
        addModules(new KunWorkflowServerModule(props), new DatabaseModule(), new SchedulerModule());
    }

    @Before
    public void setUp() {
        new Thread(() -> {
            webServer.start();
            logger.info("Webserver exited");
        }).start();
        await().atMost(30, TimeUnit.SECONDS)
                .until(() -> {
                    logger.info("Webserver is {} at {}", webServer.isReady() ? "running" : "stopped", getBaseUrl());
                    return isAvailable();
                });
    }

    public String getBaseUrl() {
        String port = props.getString(ConfigurationKeys.PROP_SERVER_PORT, "8088");
        return "http://localhost:" + port;
    }

    @After
    public void tearDown() {
        webServer.shutdown();
    }

    private boolean isAvailable() {
        String healthCheck = getBaseUrl() + "/health";
        Call call = okHttpClient.newCall(new Request.Builder().url(healthCheck).get().build());
        try (Response response = call.execute()) {
            return response.code() == 200;
        } catch (IOException e) {
            logger.warn("Resource {} is not available ", getBaseUrl());
            return false;
        }
    }
}