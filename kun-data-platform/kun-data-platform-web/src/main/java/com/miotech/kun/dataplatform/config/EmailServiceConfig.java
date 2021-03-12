package com.miotech.kun.dataplatform.config;

import com.miotech.kun.dataplatform.notify.service.EmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(value = "testenv", havingValue = "false", matchIfMissing = true)
public class EmailServiceConfig {
    @Value("${notify.email.smtpHost}")
    private String smtpHost;

    @Value("${notify.email.smtpPort:25}")
    private Integer smtpPort;

    @Value("${notify.email.smtpUsername}")
    private String smtpUsername;

    @Value("${notify.email.smtpPassword}")
    private String smtpPassword;

    @Value("${notify.email.emailFrom}")
    private String emailFrom;

    @Value("${notify.email.emailFromName:Kun Notification}")
    private String emailFromName;

    @Value("${notify.email.smtpSecurity:auto}")
    private String smtpSecurityProtocol;

    @Bean
    public EmailService createEmailService() {
        return EmailService.newBuilder()
                .withSmtpHost(smtpHost)
                .withSmtpPort(smtpPort)
                .withSmtpUserName(smtpUsername)
                .withSmtpPassword(smtpPassword)
                .withEmailFrom(emailFrom)
                .withEmailFromName(emailFromName)
                .withSecurityProtocol(smtpSecurityProtocol)
                .build();
    }
}
