package com.miotech.kun.dataquality;

import com.google.common.collect.Lists;
import com.miotech.kun.commons.pubsub.publish.EventPublisher;
import com.miotech.kun.commons.pubsub.publish.NopEventPublisher;
import com.miotech.kun.commons.pubsub.subscribe.EventSubscriber;
import com.miotech.kun.commons.testing.KunAppTestBase;
import com.miotech.kun.dataquality.mock.MockSubscriber;
import com.miotech.kun.monitor.facade.alert.NotifyFacade;
import com.miotech.kun.workflow.client.WorkflowClient;
import com.miotech.kun.workflow.client.model.ConfigKey;
import com.miotech.kun.workflow.client.model.Operator;
import com.miotech.kun.workflow.client.operator.OperatorUpload;
import com.miotech.kun.workflow.core.execution.ConfigDef;
import com.miotech.kun.workflow.utils.WorkflowIdGenerator;
import lombok.extern.slf4j.Slf4j;
import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.doReturn;

@SpringBootTest(classes = DataQualityTestBase.TestConfiguration.class)
@Slf4j
public abstract class DataQualityTestBase extends KunAppTestBase {

    @Configuration
    @EnableAutoConfiguration
    @ComponentScan(basePackages = {
            "com.miotech.kun.common",
            "com.miotech.kun.dataquality",
            "com.miotech.kun.workflow.operator"
    })
    public static class TestConfiguration {
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
            return Mockito.mock(WorkflowClient.class);
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

        @Bean
        public NotifyFacade getNotifyFacade() {
            return Mockito.mock(NotifyFacade.class);
        }

    }
}
