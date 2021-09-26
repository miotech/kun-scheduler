package com.miotech.kun.monitor.alert.notifier;

import com.miotech.kun.commons.pubsub.event.Event;
import com.miotech.kun.monitor.alert.service.EmailService;
import com.miotech.kun.monitor.facade.model.alert.EmailNotifierUserConfig;

public class EmailNotifier implements MessageNotifier {
    private final EmailService emailService;

    private final EmailNotifierUserConfig userConfig;

    public EmailNotifier(EmailService emailService, EmailNotifierUserConfig userConfig) {
        this.emailService = emailService;
        this.userConfig = userConfig;
    }

    @Override
    public void notify(Long workflowTaskId, String subject, String message) {
        emailService.sendEmail(subject, message, this.userConfig);
    }

    @Override
    public void notifyTaskStatusChange(Event event) {
        emailService.sendEmailByEventAndUserConfig(event, this.userConfig);
    }
}
