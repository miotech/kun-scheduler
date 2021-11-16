package com.miotech.kun.dataquality;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.miotech.kun.commons.pubsub.publish.EventPublisher;
import com.miotech.kun.commons.pubsub.publish.NopEventPublisher;
import com.miotech.kun.commons.pubsub.subscribe.EventSubscriber;
import com.miotech.kun.dataquality.mock.MockSubscriber;
import com.miotech.kun.workflow.client.WorkflowClient;
import com.miotech.kun.workflow.client.model.ConfigKey;
import com.miotech.kun.workflow.client.model.Operator;
import com.miotech.kun.workflow.client.model.Task;
import com.miotech.kun.workflow.client.model.TaskRun;
import com.miotech.kun.workflow.client.operator.OperatorUpload;
import com.miotech.kun.workflow.core.execution.ConfigDef;
import com.miotech.kun.workflow.utils.WorkflowIdGenerator;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = DataQualityTestBase.Configuration.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Slf4j
public abstract class DataQualityTestBase {

    private List<String> userTables;

    @Autowired
    private DataSource dataSource;

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    @After
    public void tearDown() {
        truncateAllTables();
    }

    private void truncateAllTables() {
        for (String t : inferUserTables(dataSource)) {
            jdbcTemplate.update(String.format("TRUNCATE TABLE %s;", t));
        }
    }

    private List<String> inferUserTables(DataSource dataSource) {
        if (userTables != null) {
            return userTables;
        }

        try (Connection conn = dataSource.getConnection()) {
            List<String> tables = Lists.newArrayList();
            ResultSet rs = conn.getMetaData()
                    .getTables(null, null, "%", new String[]{"TABLE"});
            while (rs.next()) {
                String tableName = rs.getString(3);
                if (tableName.startsWith("kun_dq")) {
                    tables.add(tableName);
                }
            }
            userTables = ImmutableList.copyOf(tables);
            return userTables;
        } catch (SQLException e) {
            log.error("Failed to establish connection.", e);
            throw new RuntimeException(e);
        }
    }

    @org.springframework.context.annotation.Configuration
    @EnableAutoConfiguration
    @ComponentScan(basePackages = {
            "com.miotech.kun.common",
            "com.miotech.kun.dataquality",
            "com.miotech.kun.workflow.operator"
    })
    public static class Configuration {
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

        @Bean
        public WorkflowClient getWorkflowClient() {
            WorkflowClient mockClient = Mockito.mock(WorkflowClient.class);

            doAnswer(invocation -> {
                Long taskId = invocation.getArgument(0,Long.class);
                TaskRun taskRun = TaskRun.newBuilder().withTask(Task.newBuilder().withId(taskId).build())
                        .withId(WorkflowIdGenerator.nextTaskRunId()).build();
                return taskRun;
            }).when(mockClient).executeTask(anyLong(),any());

            doAnswer(invocation -> {
                Task task = invocation.getArgument(0,Task.class);
                Task createdTask = task.cloneBuilder().withId(WorkflowIdGenerator.nextTaskId()).build();
                return createdTask;
            }).when(mockClient).createTask(any(Task.class));

            doReturn(getMockOperator())
                    .when(mockClient)
                    .saveOperator(anyString(), any());

            doReturn(Optional.of(getMockOperator())).when(mockClient).getOperator(anyString());

            doReturn(getMockOperator()).when(mockClient).getOperator(anyLong());

            return mockClient;
        }

        @Bean
        public Operator getOperator(){
            return getMockOperator();
        }

        @Bean
        public OperatorUpload getOperatorUpload() {
            OperatorUpload operatorUpload = Mockito.mock(OperatorUpload.class);
            List<Operator> mockOperators = Arrays.asList(getMockOperator());
            doReturn(mockOperators)
                    .when(operatorUpload).autoUpload();


            return operatorUpload;
        }

        @Bean("dataQuality-subscriber")
        public EventSubscriber getRedisSubscriber() {
            return new MockSubscriber();
        }

        @Bean
        public RestTemplate getRestTemplate() {
            return new RestTemplate();
        }

        @Bean("dataQuality-publisher")
        public EventPublisher getPublisher(){
            return new NopEventPublisher();
        }
    }
}
