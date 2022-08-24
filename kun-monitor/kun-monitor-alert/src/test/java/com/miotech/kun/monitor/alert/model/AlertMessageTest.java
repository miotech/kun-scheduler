package com.miotech.kun.monitor.alert.model;

import com.miotech.kun.monitor.facade.model.alert.AlertMessage;
import org.junit.jupiter.api.Test;

import static com.shazam.shazamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class AlertMessageTest {

    @Test
    public void testToContent_failure() {
        AlertMessage alertMessage = AlertMessage.newBuilder()
                .withReason(AlertMessage.AlertReason.FAILURE)
                .build();

        assertThat(alertMessage.toContent(false), is("Reason: Failure"));
        assertThat(alertMessage.toMarkdown(), is("<font color=\"warning\">Reason: Failure</font>"));
    }

    @Test
    public void testToContent_overdue() {
        AlertMessage alertMessage = AlertMessage.newBuilder()
                .withReason(AlertMessage.AlertReason.OVERDUE)
                .build();

        assertThat(alertMessage.toContent(false), is("Reason: Overdue"));
        assertThat(alertMessage.toMarkdown(), is("<font color=\"warning\">Reason: Overdue</font>"));
    }

    @Test
    public void testToContent_notification() {
        AlertMessage alertMessage = AlertMessage.newBuilder()
                .withReason(AlertMessage.AlertReason.NOTIFICATION)
                .build();

        assertThat(alertMessage.toContent(false), is("Reason: Notification"));
        assertThat(alertMessage.toMarkdown(), is("<font color=\"info\">Reason: Notification</font>"));
    }

    @Test
    public void testToContent_multiParam() {
        AlertMessage alertMessage = AlertMessage.newBuilder()
                .withReason(AlertMessage.AlertReason.NOTIFICATION)
                .withDataset("dev.test")
                .withTask("task")
                .withResult("result")
                .withOwner("owner")
                .withDeployer("deployer")
                .withUpstreamTask("upstream task")
                .withNumberOfContinuousFailure(1L)
                .withLink("link")
                .build();

        assertThat(alertMessage.toContent(false), is("Reason: Notification\n" +
                "Dataset: dev.test\n" +
                "Task: task\n" +
                "Result: result\n" +
                "Owner: owner\n" +
                "Deployer: deployer\n" +
                "Upstream Task: upstream task\n" +
                "Number of continuous failure: 1\n" +
                "Link: link"));
        assertThat(alertMessage.toMarkdown(), is("<font color=\"info\">Reason: Notification</font>\n" +
                "**Dataset**: dev.test\n" +
                "**Task**: task\n" +
                "**Result**: result\n" +
                "**Owner**: owner\n" +
                "**Deployer**: deployer\n" +
                "**Upstream Task**: upstream task\n" +
                "**Number of continuous failure**: 1\n" +
                "**Link**: [link](link)"));
    }

}
