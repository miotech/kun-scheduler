package com.miotech.kun.dataplatform.notify.service;

import com.miotech.kun.commons.utils.DateTimeUtils;
import com.miotech.kun.commons.utils.ExceptionUtils;
import com.miotech.kun.dataplatform.notify.userconfig.EmailNotifierUserConfig;
import com.miotech.kun.workflow.core.event.Event;
import com.miotech.kun.workflow.core.event.TaskAttemptStatusChangeEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class EmailService {
    private final String smtpHost;

    private final Integer smtpPort;

    private final String smtpUserName;

    private final String smtpPassword;

    private final String emailFrom;

    private final String emailFromName;

    public static final String STATUS_CHANGE_EMAIL_MSG_TEMPLATE = "Task \"%s\" goes from status \"%s\" to status \"%s\".\n" +
            "\n" +
            "Detailed information for further debugging:\n" +
            "\n" +
            "Task name: %s\n" +
            "Task ID: %s\n" +
            "Task Attempt ID: %s\n" +
            "Timestamp: %s\n";

    public EmailService(String smtpHost, Integer smtpPort, String smtpUsername, String smtpPassword, String emailFrom, String emailFromName) {
        this.smtpHost = smtpHost;
        this.smtpPort = smtpPort;
        this.smtpUserName = smtpUsername;
        this.smtpPassword = smtpPassword;
        this.emailFrom = emailFrom;
        this.emailFromName = emailFromName;
    }

    public void sendEmailByEventAndUserConfig(Event event, EmailNotifierUserConfig userConfig) {
        Email email = prepareEmail(userConfig.getEmailList());
        try {
            List<InternetAddress> sendToAddresses = getSendToListFromUserConfig(userConfig);
            EmailContent emailContent = parseEventToContent(event);
            email.setSubject(emailContent.getSubject());
            email.setMsg(emailContent.getContent());
            email.setTo(sendToAddresses);
            email.send();
        } catch (EmailException e) {
            log.error("Error occurs when trying to send emails to list: {}", userConfig.getEmailList());
            log.error("Email send error message: {}", e.getMessage());
            throw ExceptionUtils.wrapIfChecked(e);
        }
    }

    private List<InternetAddress> getSendToListFromUserConfig(EmailNotifierUserConfig userConfig) {
        List<InternetAddress> sendToAddresses = new ArrayList<>();
        try {
            for (String toEmail : userConfig.getEmailList()) {
                sendToAddresses.add(InternetAddress.parse(toEmail)[0]);
            }
        } catch (AddressException e) {
            log.error("Failed to parse address in email service");
            throw ExceptionUtils.wrapIfChecked(e);
        }
        // TODO: convert user list to email addresses
        return sendToAddresses;
    }

    private Email prepareEmail(List<String> emailToList) {
        Email email = new SimpleEmail();
        email.setHostName(this.smtpHost);
        email.setSmtpPort(this.smtpPort);
        // SSL/TLS port 465
        if (this.smtpPort == 465) {
            email.setSSLOnConnect(true);
            email.setSSLCheckServerIdentity(true);
        }
        // STARTTLS port 587
        else if (this.smtpPort == 587) {
            email.setStartTLSEnabled(true);
            email.setSSLCheckServerIdentity(true);
        }
        email.setAuthenticator(new DefaultAuthenticator(this.smtpUserName, this.smtpPassword));
        try {
            email.addTo(emailToList.toArray(new String[0]));
            email.setFrom(this.emailFrom, this.emailFromName);
            email.setFrom(this.emailFrom, this.emailFromName);
        } catch (EmailException e) {
            log.error("Failed to prepare email for email list: {}", emailToList);
            throw ExceptionUtils.wrapIfChecked(e);
        }

        return email;
    }

    /**
     * Parse event to email content by actual type of event
     * @param event The event object
     * @return parsed email content
     */
    private EmailContent parseEventToContent(Event event) {
        if (event instanceof TaskAttemptStatusChangeEvent) {
            TaskAttemptStatusChangeEvent e = (TaskAttemptStatusChangeEvent) event;
            String emailSubject = String.format("[KUN-ALERT] Task \"%s\" in state \"%s\"", e.getTaskName(), e.getToStatus());
            String emailContent = String.format(STATUS_CHANGE_EMAIL_MSG_TEMPLATE,
                    e.getTaskName(),
                    e.getFromStatus(),
                    e.getToStatus(),
                    e.getTaskName(),
                    e.getTaskId(),
                    e.getAttemptId(),
                    DateTimeUtils.fromTimestamp(e.getTimestamp())
            );

            return new EmailContent(emailSubject, emailContent);
        }
        // else
        throw new IllegalStateException(String.format("Unknown event type: \"%s\" for email service to handle", event.getClass().getName()));
    }

    public static class EmailContent {
        private final String subject;
        private final String content;

        public EmailContent(String subject, String content) {
            this.subject = subject;
            this.content = content;
        }

        public String getSubject() {
            return subject;
        }

        public String getContent() {
            return content;
        }
    }
}
