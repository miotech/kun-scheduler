package com.miotech.kun.dataplatform.notify;

import com.miotech.kun.dataplatform.AppTestBase;
import com.miotech.kun.dataplatform.TestEventDispatcherConfig;
import com.miotech.kun.dataplatform.common.notifyconfig.service.TaskNotifyConfigService;
import com.miotech.kun.dataplatform.model.notify.TaskNotifyConfig;
import com.miotech.kun.dataplatform.model.notify.TaskStatusNotifyTrigger;
import com.miotech.kun.dataplatform.notify.userconfig.WeComNotifierUserConfig;
import com.miotech.kun.workflow.core.event.Event;
import com.miotech.kun.workflow.core.event.TaskAttemptStatusChangeEvent;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunStatus;
import org.assertj.core.util.Lists;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class WorkflowEventDispatcherTest extends AppTestBase {
    @Autowired
    private TestEventDispatcherConfig testEventDispatcherConfig;

    @Autowired
    private TaskNotifyConfigService taskNotifyConfigService;

    @Test
    public void workflowEventSubscriber_shouldDoSubscribeAfterConstruct() {
        assertTrue(testEventDispatcherConfig.getEventPubSubListener() != null);
    }

    @Test
    public void workflowEventSubscriber_shouldNotResponseToUnrelatedEvents() {
        // 1. Prepare
        Event nonRelatedEvent = new RandomMockEvent();
        int emailNotifyInvokeCountBeforeEventArrive = testEventDispatcherConfig.getEmailServiceNotifyInvokeCount();
        int weComNotifyInvokeCountBeforeEventArrive = testEventDispatcherConfig.getWeComServiceNotifyInvokeCount();

        // 2. Process
        testEventDispatcherConfig.getEventPubSubListener().mockReceiveEventFromWorkflow(nonRelatedEvent);

        // 3. Validate
        int emailNotifyInvokeCountAfterEventArrive = testEventDispatcherConfig.getEmailServiceNotifyInvokeCount();
        int weComNotifyInvokeCountAfterEventArrive = testEventDispatcherConfig.getWeComServiceNotifyInvokeCount();

        assertThat(emailNotifyInvokeCountAfterEventArrive, is(emailNotifyInvokeCountBeforeEventArrive));
        assertThat(weComNotifyInvokeCountAfterEventArrive, is(weComNotifyInvokeCountBeforeEventArrive));
    }

    @Test
    public void workflowEventSubscriber_shouldResponseToMatchedEvent() {
        // 1. Prepare
        Long attemptId = 1234L;
        Long taskId = 1230L;

        int emailNotifyInvokeCountBeforeEventArrive = testEventDispatcherConfig.getEmailServiceNotifyInvokeCount();
        int weComNotifyInvokeCountBeforeEventArrive = testEventDispatcherConfig.getWeComServiceNotifyInvokeCount();

        // Should only trigger WeCom notifier, do not trigger email notifier
        taskNotifyConfigService.upsertTaskNotifyConfig(TaskNotifyConfig.newBuilder()
                .withWorkflowTaskId(taskId)
                .withTriggerType(TaskStatusNotifyTrigger.ON_FAIL)
                .withNotifierConfigs(Lists.newArrayList(
                        new WeComNotifierUserConfig()
                ))
                .build());
        TaskAttemptStatusChangeEvent event = new TaskAttemptStatusChangeEvent(attemptId, TaskRunStatus.RUNNING, TaskRunStatus.FAILED, "my-task-name", taskId);

        // 2. Process
        testEventDispatcherConfig
                .getEventPubSubListener()
                .mockReceiveEventFromWorkflow(event);

        // 3. Validate
        int emailNotifyInvokeCountAfterEventArrive = testEventDispatcherConfig.getEmailServiceNotifyInvokeCount();
        int weComNotifyInvokeCountAfterEventArrive = testEventDispatcherConfig.getWeComServiceNotifyInvokeCount();

        assertThat(emailNotifyInvokeCountAfterEventArrive, is(emailNotifyInvokeCountBeforeEventArrive));
        assertThat(weComNotifyInvokeCountAfterEventArrive, is(weComNotifyInvokeCountBeforeEventArrive + 1));
    }


    /**
     * A dummy mock event type to test response of listener (expect to take no effect)
     */
    private static class RandomMockEvent extends Event {
    }
}

