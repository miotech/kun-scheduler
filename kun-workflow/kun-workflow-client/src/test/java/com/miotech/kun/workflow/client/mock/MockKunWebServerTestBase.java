package com.miotech.kun.workflow.client.mock;

import com.google.inject.Injector;
import com.miotech.kun.commons.db.DatabaseModule;
import com.miotech.kun.commons.db.GraphDatabaseModule;
import com.miotech.kun.commons.rpc.RpcModule;
import com.miotech.kun.commons.testing.GuiceTestBase;
import com.miotech.kun.commons.utils.Props;
import com.miotech.kun.commons.utils.PropsUtils;
import com.miotech.kun.workflow.SchedulerModule;
import com.miotech.kun.workflow.common.constant.ConfigurationKeys;
import com.miotech.kun.workflow.core.event.Event;
import com.miotech.kun.workflow.core.publish.EventPublisher;
import com.miotech.kun.workflow.core.publish.RedisEventPublisher;
import com.miotech.kun.workflow.web.KunWorkflowServerModule;
import com.miotech.kun.workflow.web.KunWorkflowWebServer;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.junit.After;
import org.junit.ClassRule;
import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.session.SessionFactory;
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

    public static GenericContainer getRedis(){
        GenericContainer redis = new GenericContainer(REDIS_IMAGE)
                .withExposedPorts(6379);
        redis.start();
        return redis;
    }

    @ClassRule
    public static Neo4jContainer neo4jContainer = new Neo4jContainer("neo4j:3.5.20")
            .withAdminPassword("Mi0tech2020");

    private KunWorkflowWebServer webServer;

    private Props props;

    @Override
    protected void configuration() {
        super.configuration();
        Props props = PropsUtils.loadAppProps("application-test.yaml");
        int port = 18080 + (new Random()).nextInt(100);
        String redisIp = redis.getHost();
        logger.info("redisIp:" + redisIp);
        props.put("rpc.registry", "redis://" + redisIp + ":" + redis.getMappedPort(6379));
        props.put("rpc.port", 9001);
        logger.info("Start test workflow server in : localhost:{}", port);
        props.put(ConfigurationKeys.PROP_SERVER_PORT, Integer.toString(port));
        addModules(
                new KunWorkflowServerModule(props),
                new DatabaseModule(),
                new SchedulerModule(),
                new RpcModule(props)
        );
        // create Neo4j session factory since we do not include GraphDatabaseModule here
        bind(SessionFactory.class, initNeo4jSessionFactory());
        bind(EventPublisher.class, new NopEventPublisher());
    }

    public SessionFactory initNeo4jSessionFactory() {
        Configuration config = new Configuration.Builder()
                .uri(neo4jContainer.getBoltUrl())
                .connectionPoolSize(50)
                .credentials("neo4j", "Mi0tech2020")
                .build();
        return new SessionFactory(config, GraphDatabaseModule.DEFAULT_NEO4J_DOMAIN_CLASSES);
    }

    @Override
    protected void beforeInject(Injector injector) {
        // initialize database
        setUp(injector);
    }

    public void setUp(Injector injector) {
        props = injector.getInstance(Props.class);
        KunWorkflowWebServer.configureDB(injector,props);
        webServer = injector.getInstance(KunWorkflowWebServer.class);
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

    private static class NopEventPublisher implements EventPublisher {
        @Override
        public void publish(Event event) {
            // nop
        }
    }
}