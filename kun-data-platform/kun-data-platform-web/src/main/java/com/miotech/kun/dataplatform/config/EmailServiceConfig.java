package com.miotech.kun.dataplatform.config;

import com.miotech.kun.dataplatform.notify.service.EmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(value = "testenv", havingValue = "false", matchIfMissing = true)
public class EmailServiceConfig {
    @Value("${email.smtpHost}")
    private String smtpHost;

    @Value("$(email.smtpPort:25}")
    private Integer smtpPort;

    @Value("${email.smtpUsername}")
    private String smtpUsername;

    @Value("${email.smtpPassword}")
    private String smtpPassword;

    @Value("${email.emailFrom}")
    private String emailFrom;

    @Value("${email.emailFromName:Kun Notification}")
    private String emailFromName;

    @Bean
    public EmailService createEmailService() {
        return new EmailService(smtpHost, smtpPort, smtpUsername, smtpPassword, emailFrom, emailFromName);
    }
}
