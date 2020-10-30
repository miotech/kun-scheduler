package com.miotech.kun.dataplatform.config;

import com.miotech.kun.dataplatform.notify.TaskAttemptStatusChangeEventSubscriber;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TaskAttemptStatusChangeSubsriberConfig {
    @Bean
    public TaskAttemptStatusChangeEventSubscriber getSubscriber(){
        return new TaskAttemptStatusChangeEventSubscriber();
    }
}
