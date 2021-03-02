package com.miotech.kun.dataplatform.notify.service;

import com.miotech.kun.dataplatform.notify.userconfig.EmailNotifierUserConfig;
import com.miotech.kun.workflow.core.event.Event;

public class EmailService {
    private final String smtpHost;

    private final String smtpUserName;

    private final String smtpPassword;

    public EmailService(String smtpHost, String smtpUsername, String smtpPassword) {
        this.smtpHost = smtpHost;
        this.smtpUserName = smtpUsername;
        this.smtpPassword = smtpPassword;
    }

    public void sendEmailByEventAndUserConfig(Event event, EmailNotifierUserConfig userConfig) {
        // TODO: implement this
    }
}
