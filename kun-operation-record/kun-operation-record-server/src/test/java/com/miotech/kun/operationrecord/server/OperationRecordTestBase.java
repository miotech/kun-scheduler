package com.miotech.kun.operationrecord.server;

import com.miotech.kun.commons.pubsub.publish.EventPublisher;
import com.miotech.kun.commons.pubsub.subscribe.EventSubscriber;
import com.miotech.kun.commons.testing.KunAppTestBase;
import com.miotech.kun.operationrecord.server.config.TestFacadeConfig;
import lombok.extern.slf4j.Slf4j;
import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootTest(classes = OperationRecordTestBase.TestConfig.class)
@EnableScheduling
@EnableAsync
@Slf4j
public abstract class OperationRecordTestBase extends KunAppTestBase {

    @Configuration
    @EnableAutoConfiguration
    @ComponentScan(basePackages = {
            "com.miotech.kun.common",
            "com.miotech.kun.operationrecord",
    })
    @Import(TestFacadeConfig.class)
    public static class TestConfig {

        @Bean("operationRecord-publisher")
        public EventPublisher getRedisPublisher() {
            return Mockito.mock(EventPublisher.class);
        }

        @Bean("operationRecord-subscriber")
        public EventSubscriber getRedisSubscriber() {
            return Mockito.mock(EventSubscriber.class);
        }

        @Bean
        public ExpressionParser getExpressionParser() {
            return new SpelExpressionParser();
        }

        @Bean("localVariableTableParameterNameDiscoverer")
        public ParameterNameDiscoverer getParameterNameDiscoverer() {
            return new LocalVariableTableParameterNameDiscoverer();
        }
    }
}
