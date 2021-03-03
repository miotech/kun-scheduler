package com.miotech.kun.dataplatform.notify.service;

import com.miotech.kun.commons.utils.ExceptionUtils;
import com.miotech.kun.dataplatform.notify.userconfig.EmailNotifierUserConfig;
import com.miotech.kun.workflow.core.event.Event;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;

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
            email.setMsg(event.toString());
            email.setHostName(this.smtpHost);
            email.setSmtpPort(this.smtpPort);
            email.setFrom(this.emailFrom, this.emailFromName);

            List<InternetAddress> sendToAddresses = new ArrayList<>();
            for (String toEmail : userConfig.getEmailList()) {
                sendToAddresses.add(InternetAddress.parse(toEmail)[0]);
            }
            email.setTo(sendToAddresses);
            email.send();
        } catch (EmailException | AddressException e) {
            log.error("Error occurs when trying to send emails to list: {}", userConfig.getEmailList());
            log.error("Email send error message: {}", e.getMessage());
            throw ExceptionUtils.wrapIfChecked(e);
        }
    }

    private Email prepareEmail(List<String> emailToList) {
        Email email = new HtmlEmail();
        email.setHostName(this.smtpHost);
        email.setAuthenticator(new DefaultAuthenticator(this.smtpUserName, this.smtpPassword));
        try {
            email.addTo(emailToList.toArray(new String[0]));
            email.setFrom(this.emailFrom, this.emailFromName);
        } catch (EmailException e) {
            log.error("Failed to prepare email for email list: {}", emailToList);
            throw ExceptionUtils.wrapIfChecked(e);
        }

        return email;
    }
}
