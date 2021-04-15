package com.miotech.kun.dataplatform.notify.service;

import com.icegreen.greenmail.junit4.GreenMailRule;
import com.icegreen.greenmail.util.ServerSetupTest;
import com.miotech.kun.dataplatform.AppTestBase;
import com.miotech.kun.dataplatform.common.backfill.service.BackfillService;
import com.miotech.kun.dataplatform.common.deploy.service.DeployedTaskService;
import com.miotech.kun.dataplatform.model.deploy.DeployedTask;
import com.miotech.kun.dataplatform.notify.WorkflowEventDispatcher;
import com.miotech.kun.dataplatform.notify.userconfig.EmailNotifierUserConfig;
import com.miotech.kun.workflow.core.event.TaskAttemptStatusChangeEvent;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunStatus;
import org.assertj.core.util.Lists;
import org.joor.Reflect;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

@TestPropertySource(properties = {
        "notify.email.enabled=true"
})
public class EmailServiceTest extends AppTestBase {
    @Rule
    public final GreenMailRule greenMail = new GreenMailRule(ServerSetupTest.ALL);

    @MockBean
    private DeployedTaskService deployedTaskService;

    @MockBean
    private BackfillService backfillService;

    @MockBean
    private WorkflowEventDispatcher workflowEventDispatcher;

    @Autowired
    private EmailService emailService;

    private static final String TEST_SMTP_USERNAME = "kun-robot";

    private static final String TEST_SMTP_PASSWORD = "kun-robot-password";

    private static final String TEST_EMAIL_FROM = "kun-robot@kun.org";

    private static final String TEST_EMAIL_FROM_NAME = "kun-robot";

    @Before
    public void defineMockBehavior() {
        Mockito.doAnswer(invocation -> {
            return Optional.empty();
        }).when(deployedTaskService).findByWorkflowTaskId(Mockito.anyLong());

        Mockito.doAnswer(invocation -> {
            return Optional.empty();
        }).when(backfillService).findDerivedFromBackfill(Mockito.anyLong());

        greenMail.setUser(TEST_EMAIL_FROM, TEST_SMTP_USERNAME, TEST_SMTP_PASSWORD);

        // inject greenmail generated SMTP server setups into service instance dynamically
        Reflect.on(emailService)
                .set("smtpHost", greenMail.getSmtp().getServerSetup().getBindAddress())
                .set("smtpPort", greenMail.getSmtp().getPort());
    }

    @After
    public void teardown() {
        Reflect.on(emailService).set("smtpHost", "127.0.0.1").set("smtpPort", 25);
    }

    @Test
    public void testEmailService_sendByEmailListWithSingleUser_shouldWork() throws MessagingException, IOException {
        // 1. Prepare
        // 2. Process
        emailService.sendEmailByEventAndUserConfig(
                new TaskAttemptStatusChangeEvent(1234L, TaskRunStatus.RUNNING, TaskRunStatus.FAILED, "my-task-name", 1230L),
                new EmailNotifierUserConfig(Lists.newArrayList("foo@kun.org"), Lists.emptyList())
        );

        // 3. Validate
        MimeMessage[] messages = greenMail.getReceivedMessages();
        assertThat(messages.length, is(1));
        String emailContent = messages[0].getContent().toString();
        // Should include task information
        assertTrue(emailContent.contains("Task \"my-task-name\" ends in status \"FAILED\"."));
        assertTrue(emailContent.contains("Task ID: 1230"));
        assertTrue(emailContent.contains("Task Attempt ID: 1234"));
        // Link URL should not exists
        assertFalse(emailContent.contains("See link:"));
    }

    @Test
    public void testEmailService_sendByEmailListWithMultipleUsers_shouldWork() throws MessagingException, IOException {
        // 1. Prepare
        // 2. Process
        emailService.sendEmailByEventAndUserConfig(
                new TaskAttemptStatusChangeEvent(1234L, TaskRunStatus.RUNNING, TaskRunStatus.ABORTED, "my-task-name", 1230L),
                new EmailNotifierUserConfig(Lists.newArrayList("alice@kun.org", "bob@kun.org", "carl@kun.org"), Lists.emptyList())
        );

        // 3. Validate
        MimeMessage[] messages = greenMail.getReceivedMessages();
        assertThat(messages.length, is(3));
        String emailContent = messages[0].getContent().toString();
        // Should include task information
        assertTrue(emailContent.contains("Task \"my-task-name\" ends in status \"ABORTED\"."));
        assertTrue(emailContent.contains("Task ID: 1230"));
        assertTrue(emailContent.contains("Task Attempt ID: 1234"));
        // Link URL should not exists
        assertFalse(emailContent.contains("See link:"));
        // Content of all emails should be equivalent
        assertEquals(emailContent, messages[1].getContent().toString());
        assertEquals(emailContent, messages[2].getContent().toString());
    }

    @Test
    public void testEmailService_shouldNotSendAnyEmail_whenServiceNotEnabled() {
        // 1. Prepare
        Reflect.on(emailService).set("enabled", false);

        // 2. Process
        emailService.sendEmailByEventAndUserConfig(
                new TaskAttemptStatusChangeEvent(1234L, TaskRunStatus.RUNNING, TaskRunStatus.ABORTED, "my-task-name", 1230L),
                new EmailNotifierUserConfig(Lists.newArrayList("alice@kun.org", "bob@kun.org", "carl@kun.org"), Lists.emptyList())
        );

        // 3. Validate
        MimeMessage[] messages = greenMail.getReceivedMessages();
        assertThat(messages.length, is(0));

        // 4. teardown
        Reflect.on(emailService).set("enabled", true);
    }

    @Test
    public void testEmailService_shouldDisplayLinkURL_whenDeployedTask() throws MessagingException, IOException {
        // 1. Prepare
        long mockDefinitionId = 1111L;
        long mockTaskId = 456700L;
        long mockAttemptId = 456701L;
        long mockTaskRunId = 454656L;
        // Suppose the taskrun comes from a scheduled task
        Mockito.doAnswer(invocation -> {
            if ((long) invocation.getArgument(0) == mockTaskId) {
                return Optional.of(DeployedTask.newBuilder()
                        .withId(mockTaskId)
                        .withName("my-task-name")
                        .withDefinitionId(1111L)
                        .build());
            }
            return Optional.empty();
        }).when(deployedTaskService).findByWorkflowTaskId(Mockito.anyLong());

        // 2. Process
        emailService.sendEmailByEventAndUserConfig(
                new TaskAttemptStatusChangeEvent(mockAttemptId, TaskRunStatus.RUNNING, TaskRunStatus.SUCCESS, "my-task-name", 456700L),
                new EmailNotifierUserConfig(Lists.newArrayList("foo@kun.org"), Lists.emptyList())
        );

        // 3. Validate
        MimeMessage[] messages = greenMail.getReceivedMessages();
        assertThat(messages.length, is(1));
        String emailContent = messages[0].getContent().toString();
        // Should include task information
        assertTrue(emailContent.contains("Deployed task \"my-task-name\" ends in status \"SUCCESS\"."));
        assertTrue(emailContent.contains("Task ID: 456700"));
        assertTrue(emailContent.contains("Task Attempt ID: 456701"));
        // Link URL should exists
        assertTrue(emailContent.contains(String.format("See link: https://example.org/operation-center/scheduled-tasks/%s?taskRunId=%s", mockDefinitionId, mockTaskRunId)));
    }
}
