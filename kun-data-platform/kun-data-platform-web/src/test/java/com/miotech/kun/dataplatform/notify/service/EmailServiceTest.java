package com.miotech.kun.dataplatform.notify.service;

import com.icegreen.greenmail.junit4.GreenMailRule;
import com.icegreen.greenmail.util.ServerSetupTest;
import com.miotech.kun.dataplatform.AppTestBase;
import com.miotech.kun.dataplatform.notify.userconfig.EmailNotifierUserConfig;
import com.miotech.kun.workflow.core.event.TaskAttemptStatusChangeEvent;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunStatus;
import org.assertj.core.util.Lists;
import org.junit.Rule;
import org.junit.Test;

import javax.mail.internet.MimeMessage;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class EmailServiceTest extends AppTestBase {
    @Rule
    public final GreenMailRule greenMail = new GreenMailRule(ServerSetupTest.ALL);

    private static final String TEST_SMTP_USERNAME = "kun-robot";

    private static final String TEST_SMTP_PASSWORD = "kun-robot-password";

    private static final String TEST_EMAIL_FROM = "kun-robot@kun.org";

    private static final String TEST_EMAIL_FROM_NAME = "kun-robot";

    private EmailService prepareEmailService() {
        greenMail.setUser(TEST_EMAIL_FROM, TEST_SMTP_USERNAME, TEST_SMTP_PASSWORD);

        EmailService emailService = new EmailService(
                greenMail.getSmtp().getServerSetup().getBindAddress(),
                greenMail.getSmtp().getPort(),
                TEST_SMTP_USERNAME,
                TEST_SMTP_PASSWORD,
                TEST_EMAIL_FROM,
                TEST_EMAIL_FROM_NAME
        );

        return emailService;
    }

    @Test
    public void testEmailService_sendByEmailListWithSingleUser_shouldWork() {
        // 1. Prepare
        EmailService emailService = prepareEmailService();

        // 2. Process
        emailService.sendEmailByEventAndUserConfig(
                new TaskAttemptStatusChangeEvent(1234L, TaskRunStatus.RUNNING, TaskRunStatus.ABORTED, "my-task-name", 1230L),
                new EmailNotifierUserConfig(Lists.newArrayList("foo@kun.org"), Lists.emptyList())
        );

        // 3. Validate
        MimeMessage[] messages = greenMail.getReceivedMessages();
        assertThat(messages.length, is(1));
    }

    @Test
    public void testEmailService_sendByEmailListWithMultipleUsers_shouldWork() {
        // 1. Prepare
        EmailService emailService = prepareEmailService();

        // 2. Process
        emailService.sendEmailByEventAndUserConfig(
                new TaskAttemptStatusChangeEvent(1234L, TaskRunStatus.RUNNING, TaskRunStatus.ABORTED, "my-task-name", 1230L),
                new EmailNotifierUserConfig(Lists.newArrayList("alice@kun.org", "bob@kun.org", "carl@kun.org"), Lists.emptyList())
        );

        // 3. Validate
        MimeMessage[] messages = greenMail.getReceivedMessages();
        assertThat(messages.length, is(3));
    }
}
