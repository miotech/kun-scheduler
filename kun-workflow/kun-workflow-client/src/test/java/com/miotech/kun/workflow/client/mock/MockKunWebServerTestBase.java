package com.miotech.kun.workflow.client.mock;

import com.miotech.kun.commons.testing.GuiceTestBase;
import com.miotech.kun.commons.utils.Props;
import com.miotech.kun.commons.utils.PropsUtils;
import com.miotech.kun.infra.KunInfraWebModule;
import com.miotech.kun.infra.KunInfraWebServer;
import com.miotech.kun.metadata.web.KunMetadataModule;
import com.miotech.kun.workflow.common.constant.ConfigurationKeys;
import com.miotech.kun.workflow.web.KunWorkflowServerModule;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Neo4jContainer;
import org.testcontainers.utility.DockerImageName;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;

public class MockKunWebServerTestBase extends GuiceTestBase {

    private final Logger logger = LoggerFactory.getLogger(MockKunWebServerTestBase.class);

    private final OkHttpClient okHttpClient = new OkHttpClient();

    private static final DockerImageName REDIS_IMAGE = DockerImageName.parse("redis:6.0.8");

    public static GenericContainer redis = getRedis();

    public static GenericContainer getRedis() {
        GenericContainer redis = new GenericContainer(REDIS_IMAGE)
                .withExposedPorts(6379);
        redis.start();
        return redis;
    }

    @ClassRule
    public static Neo4jContainer neo4jContainer = new Neo4jContainer("neo4j:3.5.20")
            .withAdminPassword("Mi0tech2020");

    private KunInfraWebServer webServer;
    private Props props;

    @Override
    protected void configuration() {
        super.configuration();
        Props props = PropsUtils.loadAppProps("application-test.yaml");
        fill(props);

        int port = 18080 + (new Random()).nextInt(100);
        logger.info("Start test workflow server in : localhost:{}", port);
        props.put(ConfigurationKeys.PROP_SERVER_PORT, Integer.toString(port));
        addModules(new KunWorkflowServerModule(props)
                , new KunInfraWebModule(props), new KunMetadataModule(props));
    }

    @Before
    public void setUp() {
        props = injector.getInstance(Props.class);
        webServer = injector.getInstance(KunInfraWebServer.class);
        new Thread(() -> {
            webServer.start();
            logger.info("Webserver exited");
        }).start();

        await().atMost(60, TimeUnit.SECONDS)
                .until(() -> {
                    logger.info("Webserver is {} at {}", webServer.isReady() ? "running" : "stopped", getBaseUrl());
                    return isAvailable();
                });
    }

    @After
    public void tearDown() {
        webServer.shutdown();
    }

    public String getBaseUrl() {
        String port = props.getString(ConfigurationKeys.PROP_SERVER_PORT, "8088");
        return "http://localhost:" + port;
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

    private void fill(Props props) {
        props.put("neo4j.uri", neo4jContainer.getBoltUrl());
        props.put("neo4j.username", "neo4j");
        props.put("neo4j.password", "Mi0tech2020");

        String redisIp = redis.getHost();
        logger.info("redisIp:" + redisIp);
        props.put("rpc.registry", "redis://" + redisIp + ":" + redis.getMappedPort(6379));
        props.put("rpc.port", 9001);
    }

}