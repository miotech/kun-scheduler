package com.miotech.kun.workflow.web;

import com.google.inject.Inject;
import com.miotech.kun.commons.testing.GuiceTestBase;
import com.miotech.kun.commons.utils.ExceptionUtils;
import com.miotech.kun.commons.utils.Props;
import com.miotech.kun.commons.utils.PropsUtils;
import com.miotech.kun.infra.KunInfraWebModule;
import com.miotech.kun.infra.KunInfraWebServer;
import com.miotech.kun.metadata.facade.LineageServiceFacade;
import com.miotech.kun.metadata.facade.MetadataServiceFacade;
import com.miotech.kun.workflow.common.constant.ConfigurationKeys;
import com.miotech.kun.workflow.core.Executor;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.Neo4jContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import static com.miotech.kun.commons.testing.DatabaseTestBase.POSTGRES_IMAGE;
import static com.miotech.kun.commons.utils.CloseableUtils.closeIfPossible;

@Testcontainers
public class KunWebServerTestBase extends GuiceTestBase {

    private final Logger logger = LoggerFactory.getLogger(KunWebServerTestBase.class);

    @Container
    public static Neo4jContainer neo4jContainer = new Neo4jContainer("neo4j:3.5.20")
            .withoutAuthentication();

    @Container
    public static PostgreSQLContainer postgresContainer = new PostgreSQLContainer(POSTGRES_IMAGE);

    @Inject
    private KunInfraWebServer webServer;


    private Executor executor;

    @Inject
    private Props props;

    @Inject
    private DataSource dataSource;

    @Override
    protected void configuration() {
        super.configuration();
        Props props = PropsUtils.loadAppProps("application-test.yaml");
        fill(props);
        bind(MetadataServiceFacade.class, mock(MetadataServiceFacade.class));
        bind(LineageServiceFacade.class,mock(LineageServiceFacade.class));
        addModules(new KunWorkflowServerModule(props), new KunInfraWebModule(props));
    }

    @BeforeEach
    public void setUp() {
        new Thread(() -> webServer.start()).start();
        while (!webServer.isReady()) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                throw ExceptionUtils.wrapIfChecked(e);
            }
        }
    }

    @AfterEach
    public void tearDown() {
        // TODO: move these cleaning logic to a proper location
        closeIfPossible(dataSource); // close HikariDatasource
        executor = injector.getInstance(Executor.class);
        executor.shutdown();
        webServer.shutdown();
    }

    protected String post(String url, String message) {
        HttpPost postRequest = new HttpPost(buildUrl(url));
        try {
            postRequest.setEntity(new StringEntity(message));
            return request(postRequest);
        } catch (UnsupportedEncodingException e) {
            throw ExceptionUtils.wrapIfChecked(e);
        }
    }

    protected String put(String url, String message) {
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

    protected String buildUrl(String url) {
        String port = props.getString(ConfigurationKeys.PROP_SERVER_PORT, "8088");
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

            if (statusCode < 200 ||
                    statusCode >= 400) {
                logger.error("Http ERROR: " + response);
            }
            return response;
        } catch (IOException e) {
            logger.error("", e);
            throw ExceptionUtils.wrapIfChecked(e);
        }
    }

    private void fill(Props props) {
        props.put("datasource.jdbcUrl", postgresContainer.getJdbcUrl() + "&stringtype=unspecified");
        props.put("datasource.username", postgresContainer.getUsername());
        props.put("datasource.password", postgresContainer.getPassword());
        props.put("datasource.driverClassName", "org.postgresql.Driver");

        props.put("neo4j.uri", neo4jContainer.getBoltUrl());
        props.put("neo4j.username", "dummy");
        props.put("neo4j.password", "dummy");
    }

}
