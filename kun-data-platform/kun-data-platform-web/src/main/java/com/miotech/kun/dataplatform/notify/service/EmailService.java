package com.miotech.kun.dataplatform.notify.service;

import com.google.common.base.Preconditions;
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
import java.util.Objects;

@Slf4j
public class EmailService {
    private static final String SECURITY_TYPE_AUTO = "auto";

    private static final String SECURITY_TYPE_STARTTLS = "starttls";

    private static final String SECURITY_TYPE_SSL_TLS = "ssl_tls";

    private static final String SECURITY_TYPE_NONE = "none";

    private final String smtpHost;

    private final Integer smtpPort;

    private final String smtpUserName;

    private final String smtpPassword;

    private final String emailFrom;

    private final String emailFromName;

    /**
     * Security protocol to be applied. Available options: "auto", "starttls", "ssl_tls", "none"
     */
    private final String securityProtocol;

    public static final String STATUS_CHANGE_EMAIL_MSG_TEMPLATE = "Task \"%s\" goes from status \"%s\" to status \"%s\".\n" +
            "\n" +
            "Detailed information for further debugging:\n" +
            "\n" +
            "Task name: %s\n" +
            "Task ID: %s\n" +
            "Task Attempt ID: %s\n" +
            "Timestamp: %s\n";

    private EmailService(EmailServiceBuilder builder) {
        this.smtpHost = builder.smtpHost;
        this.smtpPort = builder.smtpPort;
        this.smtpUserName = builder.smtpUsername;
        this.smtpPassword = builder.smtpPassword;
        this.emailFrom = builder.emailFrom;
        this.emailFromName = builder.emailFromName;
        this.securityProtocol = builder.securityProtocol;
    }

    public static EmailServiceBuilder newBuilder() {
        return new EmailServiceBuilder();
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
        email.setAuthenticator(new DefaultAuthenticator(this.smtpUserName, this.smtpPassword));
        setupEmailSecurityProtocolConfigurations(email);

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

    private void setupEmailSecurityProtocolConfigurations(Email email) {
        switch (securityProtocol) {
            case SECURITY_TYPE_AUTO:
                // SSL/TLS port 465
                if (this.smtpPort == 465) {
                    configWithTlsSsl(email);
                }
                // STARTTLS port 587
                else if (this.smtpPort == 587) {
                    configWithStartTls(email);
                }
                break;
            case SECURITY_TYPE_SSL_TLS:
                configWithTlsSsl(email);
                break;
            case SECURITY_TYPE_STARTTLS:
                configWithStartTls(email);
                break;
            case SECURITY_TYPE_NONE:
            default:
                break;
        }
    }

    private static void configWithTlsSsl(Email email) {
        email.setSSLOnConnect(true);
        email.setSSLCheckServerIdentity(true);
    }

    private static void configWithStartTls(Email email) {
        email.setStartTLSEnabled(true);
        email.setSSLCheckServerIdentity(true);
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

    public static final class EmailServiceBuilder {
        private String smtpHost;
        private Integer smtpPort;
        private String smtpUsername;
        private String smtpPassword;
        private String emailFrom;
        private String emailFromName;
        private String securityProtocol;

        private EmailServiceBuilder() {
        }

        public EmailServiceBuilder withSmtpHost(String smtpHost) {
            this.smtpHost = smtpHost;
            return this;
        }

        public EmailServiceBuilder withSmtpPort(Integer smtpPort) {
            this.smtpPort = smtpPort;
            return this;
        }

        public EmailServiceBuilder withSmtpUserName(String smtpUserName) {
            this.smtpUsername = smtpUserName;
            return this;
        }

        public EmailServiceBuilder withSmtpPassword(String smtpPassword) {
            this.smtpPassword = smtpPassword;
            return this;
        }

        public EmailServiceBuilder withEmailFrom(String emailFrom) {
            this.emailFrom = emailFrom;
            return this;
        }

        public EmailServiceBuilder withEmailFromName(String emailFromName) {
            this.emailFromName = emailFromName;
            return this;
        }

        public EmailServiceBuilder withSecurityProtocol(String securityProtocol) {
            Preconditions.checkNotNull(securityProtocol);
            Preconditions.checkArgument(checkSecurityProtocol(securityProtocol.toLowerCase()),
                    "Illegal SMTP security protocol type: \"{}\". Expected to be one of following options: \"auto\", \"starttls\", \"ssl_tls\", \"none\"");
            this.securityProtocol = securityProtocol.toLowerCase();
            return this;
        }

        private static boolean checkSecurityProtocol(String securityProtocolString) {
            return Objects.equals(securityProtocolString, SECURITY_TYPE_AUTO) ||
                    Objects.equals(securityProtocolString, SECURITY_TYPE_NONE) ||
                    Objects.equals(securityProtocolString, SECURITY_TYPE_STARTTLS) ||
                    Objects.equals(securityProtocolString, SECURITY_TYPE_SSL_TLS);
        }

        public EmailService build() {
            return new EmailService(this);
        }
    }
}
