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

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class EmailServiceTest extends AppTestBase {
    @Rule
    public final GreenMailRule greenMail = new GreenMailRule(ServerSetupTest.ALL);

    @Test
    public void testSend() throws MessagingException {
        greenMail.setUser("dummyrobot@kun.org", "dummyrobot_password");
        greenMail.setUser("foo@kun.org", "foo_password");

        EmailService emailService = new EmailService(
                greenMail.getSmtp().getServerSetup().getBindAddress(),
                greenMail.getSmtp().getPort(),
                "dummyrobot@kun.org",
                "dummyrobot_password",
                "dummyrobot@kun.org",
                "dummyrobot"
        );

        emailService.sendEmailByEventAndUserConfig(
                new TaskAttemptStatusChangeEvent(1234L, TaskRunStatus.RUNNING, TaskRunStatus.ABORTED, "my-task-name", 1230L),
                new EmailNotifierUserConfig(Lists.newArrayList("foo@kun.org"), Lists.emptyList())
        );

        MimeMessage[] messages = greenMail.getReceivedMessages();
        assertThat(messages.length, is(1));
    }
}
