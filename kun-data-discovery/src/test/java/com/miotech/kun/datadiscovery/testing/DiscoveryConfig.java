package com.miotech.kun.datadiscovery.testing;

import com.amazonaws.services.glue.AWSGlue;
import com.miotech.kun.commons.pubsub.publish.EventPublisher;
import com.miotech.kun.commons.pubsub.publish.NopEventPublisher;
import com.miotech.kun.commons.pubsub.subscribe.EventSubscriber;
import com.miotech.kun.commons.pubsub.subscribe.NopEventSubscriber;
import org.apache.hadoop.conf.Configuration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import static org.mockito.Mockito.mock;

@TestConfiguration
public class DiscoveryConfig {


    @Bean
    public AWSGlue awsGlue() {
        return mock(AWSGlue.class);
    }

    @Bean(name = "filSystemConfiguration")
    public Configuration filSystemConfiguration() {
        return mock(Configuration.class);
    }

    @Bean("data-discovery-subscriber")
    public EventSubscriber eventSubscriber() {
        return new NopEventSubscriber();
    }

    @Bean("data-discovery-publisher")
    public EventPublisher eventPublisher() {
        return new NopEventPublisher();
    }
}
