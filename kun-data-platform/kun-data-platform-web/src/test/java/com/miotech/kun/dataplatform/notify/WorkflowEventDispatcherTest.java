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

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

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

    private final AtomicInteger emailServiceNotifyInvokeCount = new AtomicInteger(0);

    private final AtomicInteger weComServiceNotifyInvokeCount = new AtomicInteger(0);

    @Before
    public void initMocks() {
        // Mock email service behaviors
        Mockito.doAnswer(invocation -> {
            emailServiceNotifyInvokeCount.incrementAndGet();
            return null;
        })
                .when(mockEmailService)
                .sendEmailByEventAndUserConfig(Mockito.isA(Event.class), Mockito.isA(EmailNotifierUserConfig.class));

        // Mock WeCom service behaviors
        Mockito.doAnswer(invocation -> {
            weComServiceNotifyInvokeCount.incrementAndGet();
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
        int emailNotifyInvokeCountBeforeEventArrive = this.emailServiceNotifyInvokeCount.get();
        int weComNotifyInvokeCountBeforeEventArrive = this.weComServiceNotifyInvokeCount.get();

        // 2. Process
        eventPubSubListener.mockReceiveEventFromWorkflow(nonRelatedEvent);

        // 3. Validate
        int emailNotifyInvokeCountAfterEventArrive = this.emailServiceNotifyInvokeCount.get();
        int weComNotifyInvokeCountAfterEventArrive = this.weComServiceNotifyInvokeCount.get();

        assertThat(emailNotifyInvokeCountAfterEventArrive, is(emailNotifyInvokeCountBeforeEventArrive));
        assertThat(weComNotifyInvokeCountAfterEventArrive, is(weComNotifyInvokeCountBeforeEventArrive));
    }

    @Test
    public void workflowEventSubscriber_shouldNotifyFailedEventCorrectly() {
        // 1. Prepare
        Long attemptId = 1L;
        Long taskId = 1230L;

        int emailNotifyInvokeCountBeforeEventArrive = this.emailServiceNotifyInvokeCount.get();
        int weComNotifyInvokeCountBeforeEventArrive = this.weComServiceNotifyInvokeCount.get();

        // Expect to trigger both notifiers on failed event
        prepareTaskNotifyConfig(taskId, TaskStatusNotifyTrigger.ON_FAIL);
        TaskAttemptStatusChangeEvent eventSuccess = new TaskAttemptStatusChangeEvent(attemptId, TaskRunStatus.RUNNING, TaskRunStatus.SUCCESS, "my-task-name", taskId);
        TaskAttemptStatusChangeEvent eventFailed = new TaskAttemptStatusChangeEvent(attemptId, TaskRunStatus.RUNNING, TaskRunStatus.FAILED, "my-task-name", taskId);

        // 2. Process
        mockReceiveEventFromWorkflow(eventSuccess);
        int emailNotifyInvokeCountAfterEventSuccessArrive = this.emailServiceNotifyInvokeCount.get();
        int weComNotifyInvokeCountAfterEventSuccessArrive = this.weComServiceNotifyInvokeCount.get();

        mockReceiveEventFromWorkflow(eventFailed);
        int emailNotifyInvokeCountAfterEventFailedArrive = this.emailServiceNotifyInvokeCount.get();
        int weComNotifyInvokeCountAfterEventFailedArrive = this.weComServiceNotifyInvokeCount.get();

        // 3. Validate
        assertThat(emailNotifyInvokeCountAfterEventSuccessArrive, is(emailNotifyInvokeCountBeforeEventArrive));
        assertThat(weComNotifyInvokeCountAfterEventSuccessArrive, is(weComNotifyInvokeCountBeforeEventArrive));
        assertThat(emailNotifyInvokeCountAfterEventFailedArrive, is(emailNotifyInvokeCountBeforeEventArrive + 1));
        assertThat(weComNotifyInvokeCountAfterEventFailedArrive, is(weComNotifyInvokeCountBeforeEventArrive + 1));
    }

    @Test
    public void workflowEventSubscriber_shouldNotifySuccessEventCorrectly() {
        // 1. Prepare
        Long attemptId = 2L;
        Long taskId = 1231L;

        int emailNotifyInvokeCountBeforeEventArrive = this.emailServiceNotifyInvokeCount.get();
        int weComNotifyInvokeCountBeforeEventArrive = this.weComServiceNotifyInvokeCount.get();

        // Expect to trigger both notifiers on success event
        prepareTaskNotifyConfig(taskId, TaskStatusNotifyTrigger.ON_SUCCESS);
        TaskAttemptStatusChangeEvent eventSuccess = new TaskAttemptStatusChangeEvent(attemptId, TaskRunStatus.RUNNING, TaskRunStatus.SUCCESS, "my-task-name", taskId);
        TaskAttemptStatusChangeEvent eventFailed = new TaskAttemptStatusChangeEvent(attemptId, TaskRunStatus.RUNNING, TaskRunStatus.FAILED, "my-task-name", taskId);

        // 2. Process
        mockReceiveEventFromWorkflow(eventSuccess);
        int emailNotifyInvokeCountAfterEventSuccessArrive = this.emailServiceNotifyInvokeCount.get();
        int weComNotifyInvokeCountAfterEventSuccessArrive = this.weComServiceNotifyInvokeCount.get();

        mockReceiveEventFromWorkflow(eventFailed);
        int emailNotifyInvokeCountAfterEventFailedArrive = this.emailServiceNotifyInvokeCount.get();
        int weComNotifyInvokeCountAfterEventFailedArrive = this.weComServiceNotifyInvokeCount.get();

        // 3. Validate
        assertThat(emailNotifyInvokeCountAfterEventSuccessArrive, is(emailNotifyInvokeCountBeforeEventArrive + 1));
        assertThat(weComNotifyInvokeCountAfterEventSuccessArrive, is(weComNotifyInvokeCountBeforeEventArrive + 1));
        // should not do notification on failed event
        assertThat(emailNotifyInvokeCountAfterEventFailedArrive, is(emailNotifyInvokeCountAfterEventSuccessArrive));
        assertThat(weComNotifyInvokeCountAfterEventFailedArrive, is(weComNotifyInvokeCountAfterEventSuccessArrive));
    }

    @Test
    public void workflowEventSubscriber_shouldNotifyFinishEventCorrectly() {
        // 1. Prepare
        Long attemptId = 3L;
        Long taskId = 1232L;

        int emailNotifyInvokeCountBeforeEventArrive = this.emailServiceNotifyInvokeCount.get();
        int weComNotifyInvokeCountBeforeEventArrive = this.weComServiceNotifyInvokeCount.get();

        // Expect to trigger both notifiers on success/failed event
        prepareTaskNotifyConfig(taskId, TaskStatusNotifyTrigger.ON_FINISH);
        TaskAttemptStatusChangeEvent eventSuccess = new TaskAttemptStatusChangeEvent(attemptId, TaskRunStatus.RUNNING, TaskRunStatus.SUCCESS, "my-task-name", taskId);
        TaskAttemptStatusChangeEvent eventFailed = new TaskAttemptStatusChangeEvent(attemptId, TaskRunStatus.RUNNING, TaskRunStatus.FAILED, "my-task-name", taskId);

        // 2. Process
        mockReceiveEventFromWorkflow(eventSuccess);
        int emailNotifyInvokeCountAfterEventSuccessArrive = this.emailServiceNotifyInvokeCount.get();
        int weComNotifyInvokeCountAfterEventSuccessArrive = this.weComServiceNotifyInvokeCount.get();

        mockReceiveEventFromWorkflow(eventFailed);
        int emailNotifyInvokeCountAfterEventFailedArrive = this.emailServiceNotifyInvokeCount.get();
        int weComNotifyInvokeCountAfterEventFailedArrive = this.weComServiceNotifyInvokeCount.get();

        // 3. Validate
        assertThat(emailNotifyInvokeCountAfterEventSuccessArrive, is(emailNotifyInvokeCountBeforeEventArrive + 1));
        assertThat(weComNotifyInvokeCountAfterEventSuccessArrive, is(weComNotifyInvokeCountBeforeEventArrive + 1));
        assertThat(emailNotifyInvokeCountAfterEventFailedArrive, is(emailNotifyInvokeCountBeforeEventArrive + 2));
        assertThat(weComNotifyInvokeCountAfterEventFailedArrive, is(weComNotifyInvokeCountBeforeEventArrive + 2));
    }

    @Test
    public void workflowEventSubscriber_shouldNotNotifyEvent_whenTriggerTypeIsNever() {
        // 1. Prepare
        Long attemptId = 3L;
        Long taskId = 1232L;

        int emailNotifyInvokeCountBeforeEventArrive = this.emailServiceNotifyInvokeCount.get();
        int weComNotifyInvokeCountBeforeEventArrive = this.weComServiceNotifyInvokeCount.get();

        // Expect not to trigger any notifier
        prepareTaskNotifyConfig(taskId, TaskStatusNotifyTrigger.NEVER);
        TaskAttemptStatusChangeEvent eventSuccess = new TaskAttemptStatusChangeEvent(attemptId, TaskRunStatus.RUNNING, TaskRunStatus.SUCCESS, "my-task-name", taskId);
        TaskAttemptStatusChangeEvent eventFailed = new TaskAttemptStatusChangeEvent(attemptId, TaskRunStatus.RUNNING, TaskRunStatus.FAILED, "my-task-name", taskId);

        // 2. Process
        mockReceiveEventFromWorkflow(eventSuccess);
        int emailNotifyInvokeCountAfterEventSuccessArrive = this.emailServiceNotifyInvokeCount.get();
        int weComNotifyInvokeCountAfterEventSuccessArrive = this.weComServiceNotifyInvokeCount.get();

        mockReceiveEventFromWorkflow(eventFailed);
        int emailNotifyInvokeCountAfterEventFailedArrive = this.emailServiceNotifyInvokeCount.get();
        int weComNotifyInvokeCountAfterEventFailedArrive = this.weComServiceNotifyInvokeCount.get();

        // 3. Validate
        assertThat(emailNotifyInvokeCountAfterEventSuccessArrive, is(emailNotifyInvokeCountBeforeEventArrive));
        assertThat(weComNotifyInvokeCountAfterEventSuccessArrive, is(weComNotifyInvokeCountBeforeEventArrive));
        assertThat(emailNotifyInvokeCountAfterEventFailedArrive, is(emailNotifyInvokeCountBeforeEventArrive));
        assertThat(weComNotifyInvokeCountAfterEventFailedArrive, is(weComNotifyInvokeCountBeforeEventArrive));
    }

    private void mockReceiveEventFromWorkflow(Event event) {
        eventPubSubListener.mockReceiveEventFromWorkflow(event);
    }

    private void prepareTaskNotifyConfig(Long taskId, TaskStatusNotifyTrigger triggerType) {
        taskNotifyConfigService.upsertTaskNotifyConfig(TaskNotifyConfig.newBuilder()
                .withWorkflowTaskId(taskId)
                .withTriggerType(triggerType)
                .withNotifierConfigs(Lists.newArrayList(
                        new WeComNotifierUserConfig(),
                        new EmailNotifierUserConfig(Collections.EMPTY_LIST, Collections.EMPTY_LIST)
                ))
                .build());
    }

    public void workflowEventDispatcher_shouldOnlyDispatchWecomNotifier_whenOnlyWeComNotifierEnabled() {
        // 1. Prepare
        Long attemptId = 2L;
        Long taskId = 1233L;

        int emailNotifyInvokeCountBeforeEventArrive = this.emailServiceNotifyInvokeCount.get();
        int weComNotifyInvokeCountBeforeEventArrive = this.weComServiceNotifyInvokeCount.get();

        // Should only trigger only WeCom notifier, do not trigger email notifier
        taskNotifyConfigService.upsertTaskNotifyConfig(TaskNotifyConfig.newBuilder()
                .withWorkflowTaskId(taskId)
                .withTriggerType(TaskStatusNotifyTrigger.ON_FINISH)
                .withNotifierConfigs(Lists.newArrayList(
                        new WeComNotifierUserConfig()
                ))
                .build());
        TaskAttemptStatusChangeEvent eventSuccess = new TaskAttemptStatusChangeEvent(attemptId, TaskRunStatus.RUNNING, TaskRunStatus.SUCCESS, "my-task-name", taskId);
        TaskAttemptStatusChangeEvent eventFailed = new TaskAttemptStatusChangeEvent(attemptId, TaskRunStatus.RUNNING, TaskRunStatus.FAILED, "my-task-name", taskId);

        // 2. Process
        mockReceiveEventFromWorkflow(eventSuccess);
        int emailNotifyInvokeCountAfterEventSuccessArrive = this.emailServiceNotifyInvokeCount.get();
        int weComNotifyInvokeCountAfterEventSuccessArrive = this.weComServiceNotifyInvokeCount.get();

        mockReceiveEventFromWorkflow(eventFailed);
        int emailNotifyInvokeCountAfterEventFailedArrive = this.emailServiceNotifyInvokeCount.get();
        int weComNotifyInvokeCountAfterEventFailedArrive = this.weComServiceNotifyInvokeCount.get();

        // 3. Validate
        assertThat(emailNotifyInvokeCountAfterEventSuccessArrive, is(emailNotifyInvokeCountBeforeEventArrive));
        assertThat(weComNotifyInvokeCountAfterEventSuccessArrive, is(weComNotifyInvokeCountBeforeEventArrive + 1));
        assertThat(emailNotifyInvokeCountAfterEventFailedArrive, is(emailNotifyInvokeCountBeforeEventArrive));
        assertThat(weComNotifyInvokeCountAfterEventFailedArrive, is(weComNotifyInvokeCountBeforeEventArrive + 2));
    }

    @Test
    public void workflowEventDispatcher_shouldUseSystemDefaultConfig_whenTaskIdExplicitlyRegistered() {
        // 1. Prepare
        Long attemptId = 1234L;
        Long taskId = 1230L;

        int emailNotifyInvokeCountBeforeEventArrive = this.emailServiceNotifyInvokeCount.get();
        int weComNotifyInvokeCountBeforeEventArrive = this.weComServiceNotifyInvokeCount.get();

        // Register with system default trigger
        taskNotifyConfigService.upsertTaskNotifyConfig(TaskNotifyConfig.newBuilder()
                .withWorkflowTaskId(taskId)
                // Should goes as system default config
                .withTriggerType(TaskStatusNotifyTrigger.SYSTEM_DEFAULT)
                // no notifier config required
                .withNotifierConfigs(Lists.newArrayList())
                .build());

        TaskAttemptStatusChangeEvent eventFailed = new TaskAttemptStatusChangeEvent(attemptId, TaskRunStatus.RUNNING, TaskRunStatus.FAILED, "my-task-name", taskId);
        TaskAttemptStatusChangeEvent eventSuccess = new TaskAttemptStatusChangeEvent(attemptId, TaskRunStatus.RUNNING, TaskRunStatus.SUCCESS, "my-task-name-2", taskId);

        // 2. Process
        mockReceiveEventFromWorkflow(eventFailed);
        int emailNotifyInvokeCountAfterEventFailedArrive = this.emailServiceNotifyInvokeCount.get();
        int weComNotifyInvokeCountAfterEventFailedArrive = this.weComServiceNotifyInvokeCount.get();

        mockReceiveEventFromWorkflow(eventSuccess);
        int emailNotifyInvokeCountAfterEventSuccessArrive = this.emailServiceNotifyInvokeCount.get();
        int weComNotifyInvokeCountAfterEventSuccessArrive = this.weComServiceNotifyInvokeCount.get();

        // 3. Validate
        assertThat(emailNotifyInvokeCountAfterEventFailedArrive, is(emailNotifyInvokeCountBeforeEventArrive));
        assertThat(weComNotifyInvokeCountAfterEventFailedArrive, is(weComNotifyInvokeCountBeforeEventArrive + 1));
        assertThat(emailNotifyInvokeCountAfterEventSuccessArrive, is(emailNotifyInvokeCountBeforeEventArrive));
        assertThat(weComNotifyInvokeCountAfterEventSuccessArrive, is(weComNotifyInvokeCountBeforeEventArrive + 1));
    }

    @Test
    public void workflowEventDispatcher_shouldUseSystemDefaultConfig_whenTaskIdIsNotRegistered() {
        // 1. Prepare
        Long attemptId = 1234L;
        Long taskId = 1236L;

        int emailNotifyInvokeCountBeforeEventArrive = this.emailServiceNotifyInvokeCount.get();
        int weComNotifyInvokeCountBeforeEventArrive = this.weComServiceNotifyInvokeCount.get();

        // Here we do not register notify configurations explicitly
        // But event dispatcher is still expected to notify by system default configuration

        TaskAttemptStatusChangeEvent eventFailed = new TaskAttemptStatusChangeEvent(attemptId, TaskRunStatus.RUNNING, TaskRunStatus.FAILED, "my-task-name", taskId);
        TaskAttemptStatusChangeEvent eventSuccess = new TaskAttemptStatusChangeEvent(attemptId, TaskRunStatus.RUNNING, TaskRunStatus.SUCCESS, "my-task-name-2", taskId);

        // 2. Process
        mockReceiveEventFromWorkflow(eventFailed);
        int emailNotifyInvokeCountAfterEventFailedArrive = this.emailServiceNotifyInvokeCount.get();
        int weComNotifyInvokeCountAfterEventFailedArrive = this.weComServiceNotifyInvokeCount.get();

        mockReceiveEventFromWorkflow(eventSuccess);
        int emailNotifyInvokeCountAfterEventSuccessArrive = this.emailServiceNotifyInvokeCount.get();
        int weComNotifyInvokeCountAfterEventSuccessArrive = this.weComServiceNotifyInvokeCount.get();

        // 3. Validate
        assertThat(emailNotifyInvokeCountAfterEventFailedArrive, is(emailNotifyInvokeCountBeforeEventArrive));
        assertThat(weComNotifyInvokeCountAfterEventFailedArrive, is(weComNotifyInvokeCountBeforeEventArrive + 1));
        assertThat(emailNotifyInvokeCountAfterEventSuccessArrive, is(emailNotifyInvokeCountBeforeEventArrive));
        assertThat(weComNotifyInvokeCountAfterEventSuccessArrive, is(weComNotifyInvokeCountBeforeEventArrive + 1));
    }

    /**
     * A dummy mock event type to test response of listener (expect to take no effect)
     */
    private static class RandomMockEvent extends Event {
    }
}
