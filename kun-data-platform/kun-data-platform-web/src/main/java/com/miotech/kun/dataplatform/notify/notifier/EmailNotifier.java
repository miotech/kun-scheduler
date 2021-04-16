package com.miotech.kun.dataplatform.notify.notifier;

import com.miotech.kun.dataplatform.notify.MessageNotifier;
import com.miotech.kun.dataplatform.notify.service.EmailService;
import com.miotech.kun.dataplatform.notify.userconfig.EmailNotifierUserConfig;
import com.miotech.kun.workflow.core.event.Event;

public class EmailNotifier implements MessageNotifier {
    private final EmailService emailService;

    private final EmailNotifierUserConfig userConfig;

    public EmailNotifier(EmailService emailService, EmailNotifierUserConfig userConfig) {
        this.emailService = emailService;
        this.userConfig = userConfig;
    }

    @Override
    public void notify(Event event) {
        emailService.sendEmailByEventAndUserConfig(event, this.userConfig);
    }
}
