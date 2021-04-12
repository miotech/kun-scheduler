package com.miotech.kun.dataplatform.notify.service;

import com.miotech.kun.commons.utils.DateTimeUtils;
import com.miotech.kun.commons.utils.ExceptionUtils;
import com.miotech.kun.dataplatform.common.backfill.service.BackfillService;
import com.miotech.kun.dataplatform.common.deploy.service.DeployedTaskService;
import com.miotech.kun.dataplatform.config.NotifyLinkConfig;
import com.miotech.kun.dataplatform.notify.userconfig.EmailNotifierUserConfig;
import com.miotech.kun.workflow.core.event.Event;
import com.miotech.kun.workflow.core.event.TaskAttemptStatusChangeEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class EmailService {
    private static final String SECURITY_TYPE_AUTO = "auto";

    private static final String SECURITY_TYPE_STARTTLS = "starttls";

    private static final String SECURITY_TYPE_SSL_TLS = "ssl_tls";

    private static final String SECURITY_TYPE_NONE = "none";

    public static final String STATUS_CHANGE_EMAIL_MSG_TEMPLATE = "Task \"%s\" ends in status \"%s\".\n" +
            "\n" +
            "Detailed information for further debugging:\n" +
            "\n" +
            "Task name: %s\n" +
            "Task ID: %s\n" +
            "Task run ID: %s\n" +
            "Task Attempt ID: %s\n" +
            "Timestamp: %s\n";

    public static final String STATUS_CHANGE_EMAIL_MSG_SCHEDULED_TASK_TEMPLATE = "Deployed task \"%s\" ends in status \"%s\".\n" +
            "\n" +
            "Detailed information for further debugging:\n" +
            "\n" +
            "Task name: %s\n" +
            "Task ID: %s\n" +
            "Task run ID: %s\n" +
            "Task Attempt ID: %s\n" +
            "Timestamp: %s\n" +
            "\n"  +
            "See link: %s";

    @Autowired
    private DeployedTaskService deployedTaskService;

    @Autowired
    private BackfillService backfillService;

    @Autowired
    private NotifyLinkConfig notifyLinkConfig;

    @Value("${notify.email.enabled:false}")
    private Boolean enabled;

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

    public void sendEmailByEventAndUserConfig(Event event, EmailNotifierUserConfig userConfig) {
        if (!enabled) {
            return;
        }
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
        email.setAuthenticator(new DefaultAuthenticator(this.smtpUsername, this.smtpPassword));
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
        switch (smtpSecurityProtocol) {
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
            String emailContent;

            long taskRunId = e.getTaskRunId();
            Optional<Long> derivingBackfillId = backfillService.findDerivedFromBackfill(taskRunId);
            Optional<Long> taskDefinitionId = deployedTaskService.findByWorkflowTaskId(e.getTaskId()).map(deployedTask -> deployedTask.getDefinitionId());

            // If it is not a backfill task run, and corresponding deployment task is found, then it should be a scheduled task run
            if (taskDefinitionId.isPresent() && !derivingBackfillId.isPresent()) {
                emailContent = String.format(STATUS_CHANGE_EMAIL_MSG_SCHEDULED_TASK_TEMPLATE,
                        e.getTaskName(),
                        e.getToStatus(),
                        e.getTaskName(),
                        e.getTaskId(),
                        e.getTaskRunId(),
                        e.getAttemptId(),
                        DateTimeUtils.fromTimestamp(e.getTimestamp()),
                        generateLinkUrl(taskDefinitionId.get(), taskRunId)
                );
            } else {
                // TODO: @joshoy generate a link for backfill webpage. Should be supported by frontend UI first.
                emailContent = String.format(STATUS_CHANGE_EMAIL_MSG_TEMPLATE,
                        e.getTaskName(),
                        e.getToStatus(),
                        e.getTaskName(),
                        e.getTaskId(),
                        e.getTaskRunId(),
                        e.getAttemptId(),
                        DateTimeUtils.fromTimestamp(e.getTimestamp())
                );
            }

            return new EmailContent(emailSubject, emailContent);
        }
        // else
        throw new IllegalStateException(String.format("Unknown event type: \"%s\" for email service to handle", event.getClass().getName()));
    }

    private String generateLinkUrl(long taskDefinitionId, long taskRunId) {
        return notifyLinkConfig.getPrefix() + String.format("/operation-center/scheduled-tasks/%s?taskRunId=%s", taskDefinitionId, taskRunId);
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
