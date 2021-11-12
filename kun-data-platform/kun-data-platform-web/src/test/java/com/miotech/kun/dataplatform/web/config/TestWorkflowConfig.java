package com.miotech.kun.dataplatform.web.config;

import com.google.common.collect.Lists;
import com.miotech.kun.commons.pubsub.subscribe.EventSubscriber;
import com.miotech.kun.monitor.facade.model.alert.SystemDefaultNotifierConfig;
import com.miotech.kun.workflow.client.WorkflowClient;
import com.miotech.kun.workflow.client.model.ConfigKey;
import com.miotech.kun.workflow.client.model.Operator;
import com.miotech.kun.workflow.client.operator.OperatorUpload;
import com.miotech.kun.workflow.core.execution.ConfigDef;
import com.miotech.kun.workflow.utils.WorkflowIdGenerator;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.containers.Neo4jContainer;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doReturn;

@TestConfiguration
public class TestWorkflowConfig {
    private Operator getMockOperator() {
        ConfigKey configKey = new ConfigKey();
        configKey.setName("sparkSQL");
        configKey.setDisplayName("sql");
        configKey.setReconfigurable(true);
        configKey.setType(ConfigDef.Type.STRING);
        Operator operator = Operator.newBuilder()
                .withId(WorkflowIdGenerator.nextOperatorId())
                .withName("SparkSQL")
                .withClassName("com.miotech.kun.dataplatform.mocking.TestSQLOperator")
                .withConfigDef(Lists.newArrayList(configKey))
                .withDescription("Spark SQL Operator")
                .build();
        return operator;
    }

    @Bean("dataPlatformNeo4jDataSource")
    public DataSource getNeo4jDataSource() {
        Neo4jContainer neo4jContainer = new Neo4jContainer("neo4j:3.5.20")
                .withoutAuthentication();
        neo4jContainer.start();

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(String.format("jdbc:neo4j:bolt://%s:%s", neo4jContainer.getHost(), neo4jContainer.getFirstMappedPort()));
        config.setDriverClassName("org.neo4j.jdbc.bolt.BoltDriver");
        return new HikariDataSource(config);
    }

    @Bean(name = "neo4jJdbcTemplate")
    public JdbcTemplate neo4jJdbcTemplate(@Qualifier("dataPlatformNeo4jDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean
    public WorkflowClient getWorkflowClient() {
        WorkflowClient mockClient = Mockito.mock(WorkflowClient.class);

        doReturn(getMockOperator())
                .when(mockClient)
                .saveOperator(anyString(), any());

        doReturn(Optional.of(getMockOperator())).when(mockClient).getOperator(anyString());

        doReturn(getMockOperator()).when(mockClient).getOperator(anyLong());

        return mockClient;
    }

    @Bean
    public OperatorUpload getOperatorUpload() {
        OperatorUpload operatorUpload = Mockito.mock(OperatorUpload.class);
        List<Operator> mockOperators = Arrays.asList(getMockOperator());
        doReturn(mockOperators)
                .when(operatorUpload).autoUpload();


        return operatorUpload;
    }

    @Bean("alert-subscriber")
    public EventSubscriber getEventSubscriber() {
        return Mockito.mock(EventSubscriber.class);
    }

    @Bean
    public SystemDefaultNotifierConfig getSystemDefaultNotifierConfig() {
        return Mockito.mock(SystemDefaultNotifierConfig.class);
    }

    @Bean("sla-subscriber")
    public EventSubscriber getRedisSubscriber() {
        return Mockito.mock(EventSubscriber.class);
    }

}
