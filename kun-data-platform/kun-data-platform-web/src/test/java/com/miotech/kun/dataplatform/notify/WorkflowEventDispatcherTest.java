package com.miotech.kun.dataplatform.notify;

import com.fasterxml.jackson.core.type.TypeReference;
import com.miotech.kun.dataplatform.AppTestBase;
import com.miotech.kun.dataplatform.common.notifyconfig.service.TaskNotifyConfigService;
import com.miotech.kun.dataplatform.model.notify.TaskNotifyConfig;
import com.miotech.kun.dataplatform.model.notify.TaskStatusNotifyTrigger;
import com.miotech.kun.dataplatform.notify.service.EmailService;
import com.miotech.kun.dataplatform.notify.service.WeComService;
import com.miotech.kun.dataplatform.notify.userconfig.EmailNotifierUserConfig;
import com.miotech.kun.dataplatform.notify.userconfig.NotifierUserConfig;
import com.miotech.kun.dataplatform.notify.userconfig.WeComNotifierUserConfig;
import com.miotech.kun.workflow.core.event.Event;
import com.miotech.kun.workflow.core.event.EventReceiver;
import com.miotech.kun.workflow.core.event.TaskAttemptStatusChangeEvent;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunStatus;
import com.miotech.kun.workflow.core.publish.EventSubscriber;
import com.miotech.kun.workflow.utils.JSONUtils;
import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@Import(WorkflowEventDispatcherTest.WorkflowEventDispatcherTestConfig.class)
public class WorkflowEventDispatcherTest extends AppTestBase {

    private static volatile MockWorkflowEventPubSubListener eventPubSubListener = null;

    @TestConfiguration
    public static class WorkflowEventDispatcherTestConfig {
        @Value("${notify.systemDefault.triggerType}")
        private String systemDefaultConfigTriggerTypeStr;

        @Value("${notify.systemDefault.userConfigJson}")
        private String systemDefaultNotifierConfigJson;

        @Bean
        @Primary
        public SystemDefaultNotifierConfig createTestSystemNotifierConfig() {
            TaskStatusNotifyTrigger triggerType = TaskStatusNotifyTrigger.from(systemDefaultConfigTriggerTypeStr);
            List<NotifierUserConfig> systemDefaultNotifierConfig =
                    JSONUtils.jsonToObject(systemDefaultNotifierConfigJson, new TypeReference<List<NotifierUserConfig>>() {});
            return new SystemDefaultNotifierConfig(triggerType, systemDefaultNotifierConfig);
        }

        @Bean
        @Primary
        public EventSubscriber createMockEventSubscriber() {
            EventSubscriber mockEventSubscriber = Mockito.mock(EventSubscriber.class);
            Mockito.doAnswer(invocation -> {
                eventPubSubListener = new MockWorkflowEventPubSubListener(invocation.getArgument(0));
                return null;
            }).when(mockEventSubscriber).subscribe(Mockito.isA(EventReceiver.class));
            return mockEventSubscriber;
        }

        @Bean
        @Primary
        public WorkflowEventDispatcher createWorkflowEventDispatcher() {
            // Create a real workflow event dispatcher but mocks all its dependencies
            return new WorkflowEventDispatcher();
        }
    }

    @MockBean
    private EmailService mockEmailService;

    @MockBean
    private WeComService mockWeComService;

    @Autowired
    private EventSubscriber mockEventSubscriber;

    @Autowired
    private TaskNotifyConfigService taskNotifyConfigService;

    private volatile int emailServiceNotifyInvokeCount = 0;

    private volatile int weComServiceNotifyInvokeCount = 0;

    @Before
    public void initMocks() {
        // Mock email service behaviors
        Mockito.doAnswer(invocation -> {
            emailServiceNotifyInvokeCount += 1;
            return null;
        })
                .when(mockEmailService)
                .sendEmailByEventAndUserConfig(Mockito.isA(Event.class), Mockito.isA(EmailNotifierUserConfig.class));

        // Mock WeCom service behaviors
        Mockito.doAnswer(invocation -> {
            weComServiceNotifyInvokeCount += 1;
            return null;
        })
                .when(mockWeComService)
                .sendMessage(Mockito.isA(TaskAttemptStatusChangeEvent.class));
    }

    @Test
    public void workflowEventSubscriber_shouldDoSubscribeAfterConstruct() {
        assertTrue(eventPubSubListener != null);
    }

    @Test
    public void workflowEventSubscriber_shouldNotResponseToUnrelatedEvents() {
        // 1. Prepare
        Event nonRelatedEvent = new RandomMockEvent();
        int emailNotifyInvokeCountBeforeEventArrive = this.emailServiceNotifyInvokeCount;
        int weComNotifyInvokeCountBeforeEventArrive = this.weComServiceNotifyInvokeCount;

        // 2. Process
        eventPubSubListener.mockReceiveEventFromWorkflow(nonRelatedEvent);

        // 3. Validate
        int emailNotifyInvokeCountAfterEventArrive = this.emailServiceNotifyInvokeCount;
        int weComNotifyInvokeCountAfterEventArrive = this.weComServiceNotifyInvokeCount;

        assertThat(emailNotifyInvokeCountAfterEventArrive, is(emailNotifyInvokeCountBeforeEventArrive));
        assertThat(weComNotifyInvokeCountAfterEventArrive, is(weComNotifyInvokeCountBeforeEventArrive));
    }

    @Test
    public void workflowEventSubscriber_shouldResponseToMatchedEvent() {
        // 1. Prepare
        Long attemptId = 1234L;
        Long taskId = 1230L;

        int emailNotifyInvokeCountBeforeEventArrive = this.emailServiceNotifyInvokeCount;
        int weComNotifyInvokeCountBeforeEventArrive = this.weComServiceNotifyInvokeCount;

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
        mockReceiveEventFromWorkflow(event);

        // 3. Validate
        int emailNotifyInvokeCountAfterEventArrive = this.emailServiceNotifyInvokeCount;
        int weComNotifyInvokeCountAfterEventArrive = this.weComServiceNotifyInvokeCount;

        assertThat(emailNotifyInvokeCountAfterEventArrive, is(emailNotifyInvokeCountBeforeEventArrive));
        assertThat(weComNotifyInvokeCountAfterEventArrive, is(weComNotifyInvokeCountBeforeEventArrive + 1));
    }


    private void mockReceiveEventFromWorkflow(Event event) {
        eventPubSubListener.mockReceiveEventFromWorkflow(event);
    }

    @Test
    public void workflowEventDispatcher_shouldUseSystemDefaultConfigProperly() {
        // 1. Prepare
        Long attemptId = 1234L;
        Long taskId = 1230L;

        int emailNotifyInvokeCountBeforeEventArrive = this.emailServiceNotifyInvokeCount;
        int weComNotifyInvokeCountBeforeEventArrive = this.weComServiceNotifyInvokeCount;

        taskNotifyConfigService.upsertTaskNotifyConfig(TaskNotifyConfig.newBuilder()
                .withWorkflowTaskId(taskId)
                // Should goes as system default config
                .withTriggerType(TaskStatusNotifyTrigger.SYSTEM_DEFAULT)
                // no notifier config required
                .withNotifierConfigs(Lists.newArrayList())
                .build());

        TaskAttemptStatusChangeEvent event = new TaskAttemptStatusChangeEvent(attemptId, TaskRunStatus.RUNNING, TaskRunStatus.FAILED, "my-task-name", taskId);
        TaskAttemptStatusChangeEvent event2 = new TaskAttemptStatusChangeEvent(attemptId, TaskRunStatus.RUNNING, TaskRunStatus.SUCCESS, "my-task-name-2", taskId);

        // 2. Process
        mockReceiveEventFromWorkflow(event);
        mockReceiveEventFromWorkflow(event2);

        // 3. Validate
        int emailNotifyInvokeCountAfterEventArrive = this.emailServiceNotifyInvokeCount;
        int weComNotifyInvokeCountAfterEventArrive = this.weComServiceNotifyInvokeCount;

        assertThat(emailNotifyInvokeCountAfterEventArrive, is(emailNotifyInvokeCountBeforeEventArrive));
        assertThat(weComNotifyInvokeCountAfterEventArrive, is(weComNotifyInvokeCountBeforeEventArrive + 1));
    }

    /**
     * A dummy mock event type to test response of listener (expect to take no effect)
     */
    private static class RandomMockEvent extends Event {
    }
}
