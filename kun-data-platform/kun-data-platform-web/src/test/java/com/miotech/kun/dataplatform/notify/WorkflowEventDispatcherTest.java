package com.miotech.kun.dataplatform.notify;

import com.fasterxml.jackson.core.type.TypeReference;
import com.miotech.kun.common.constant.DataQualityConstant;
import com.miotech.kun.dataplatform.AppTestBase;
import com.miotech.kun.dataplatform.common.backfill.dao.BackfillDao;
import com.miotech.kun.dataplatform.common.notifyconfig.service.TaskNotifyConfigService;
import com.miotech.kun.dataplatform.common.taskdefinition.dao.TaskTryDao;
import com.miotech.kun.dataplatform.mocking.MockBackfillFactory;
import com.miotech.kun.dataplatform.mocking.MockTaskDefinitionFactory;
import com.miotech.kun.dataplatform.model.backfill.Backfill;
import com.miotech.kun.dataplatform.model.notify.TaskNotifyConfig;
import com.miotech.kun.dataplatform.model.notify.TaskStatusNotifyTrigger;
import com.miotech.kun.dataplatform.model.taskdefinition.TaskTry;
import com.miotech.kun.dataplatform.notify.service.EmailService;
import com.miotech.kun.dataplatform.notify.service.WeComService;
import com.miotech.kun.dataplatform.notify.userconfig.EmailNotifierUserConfig;
import com.miotech.kun.dataplatform.notify.userconfig.NotifierUserConfig;
import com.miotech.kun.dataplatform.notify.userconfig.WeComNotifierUserConfig;
import com.miotech.kun.workflow.core.event.Event;
import com.miotech.kun.workflow.core.event.EventReceiver;
import com.miotech.kun.workflow.core.event.PrivateEvent;
import com.miotech.kun.workflow.core.event.TaskAttemptStatusChangeEvent;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunStatus;
import com.miotech.kun.workflow.core.publish.EventSubscriber;
import com.miotech.kun.workflow.utils.JSONUtils;
import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

import java.util.Collections;
import java.util.List;

import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Import(WorkflowEventDispatcherTest.WorkflowEventDispatcherTestConfig.class)
public class WorkflowEventDispatcherTest extends AppTestBase {

    public static class MockEventPubSub implements EventSubscriber {
        private EventReceiver receiver = null;

        public void post(Event event) {
            receiver.post(event);
        }

        public EventReceiver getReceiver() {
            return this.receiver;
        }

        @Override
        public void subscribe(EventReceiver receiver) {
            this.receiver = receiver;
        }
    }

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
            return new MockEventPubSub();
        }

        @Bean
        @Primary
        public WorkflowEventDispatcher createWorkflowEventDispatcher() {
            return new WorkflowEventDispatcher();
        }
    }

    @Autowired
    private TaskNotifyConfigService taskNotifyConfigService;

    @Autowired
    private BackfillDao backfillDao;

    @Autowired
    private TaskTryDao taskTryDao;

    @MockBean
    private EmailService emailService;

    @MockBean
    private WeComService weComService;

    @Autowired
    private EventSubscriber eventSubscriber;

    private MockEventPubSub mockEventPubSub;

    @Before
    public void beforeTest() {
        mockEventPubSub = (MockEventPubSub) eventSubscriber;
    }

    @Test
    public void workflowEventSubscriber_shouldDoSubscribeAfterConstruct() {
        assertNotNull(mockEventPubSub);
        assertNotNull(mockEventPubSub.getReceiver());
    }

    /**
     * A dummy mock event type to test response of listener (expect to take no effect)
     */
    private static class RandomMockEvent extends PrivateEvent {
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

    @Test
    public void workflowEventSubscriber_shouldNotResponseToUnrelatedEvents() {
        // 1. Prepare
        Event nonRelatedEvent = new RandomMockEvent();

        // 2. Process
        mockEventPubSub.post(nonRelatedEvent);

        // email & wecom service should be triggered
        verify(weComService, never()).sendMessage(any());
        verify(emailService, never()).sendEmailByEventAndUserConfig(any(), any());
    }

    @Test
    public void workflowEventSubscriber_shouldNotifyFailedEventCorrectly() {
        // 1. Prepare
        long attemptId = 1L;
        long taskId = 1230L;

        // Expect to trigger both notifiers on failed event
        prepareTaskNotifyConfig(taskId, TaskStatusNotifyTrigger.ON_FAIL);
        TaskAttemptStatusChangeEvent eventSuccess = new TaskAttemptStatusChangeEvent(attemptId, TaskRunStatus.RUNNING, TaskRunStatus.SUCCESS, "my-task-name", taskId);
        TaskAttemptStatusChangeEvent eventFailed = new TaskAttemptStatusChangeEvent(attemptId, TaskRunStatus.RUNNING, TaskRunStatus.FAILED, "my-task-name", taskId);

        // 2. Process
        mockEventPubSub.post(eventSuccess);
        mockEventPubSub.post(eventFailed);

        // 3. Validate
        ArgumentCaptor<Event> wecomServiceEventArgumentCaptor = ArgumentCaptor.forClass(Event.class);
        ArgumentCaptor<Event> emailServiceEventArgumentCaptor = ArgumentCaptor.forClass(Event.class);
        verify(weComService, times(1))
                .sendMessage(wecomServiceEventArgumentCaptor.capture());
        verify(emailService, times(1))
                .sendEmailByEventAndUserConfig(
                        emailServiceEventArgumentCaptor.capture(),
                        Mockito.eq(new EmailNotifierUserConfig(Collections.EMPTY_LIST, Collections.EMPTY_LIST))
                );
        assertThat(wecomServiceEventArgumentCaptor.getValue(), sameBeanAs(eventFailed));
        assertThat(wecomServiceEventArgumentCaptor.getValue(), sameBeanAs(emailServiceEventArgumentCaptor.getValue()));
    }

    @Test
    public void workflowEventSubscriber_shouldNotifySuccessEventCorrectly() {
        // 1. Prepare
        long attemptId = 1L;
        long taskId = 1231L;

        // Expect to trigger both notifiers on success event
        prepareTaskNotifyConfig(taskId, TaskStatusNotifyTrigger.ON_SUCCESS);
        TaskAttemptStatusChangeEvent eventSuccess = new TaskAttemptStatusChangeEvent(attemptId, TaskRunStatus.RUNNING, TaskRunStatus.SUCCESS, "my-task-name", taskId);
        TaskAttemptStatusChangeEvent eventFailed = new TaskAttemptStatusChangeEvent(attemptId, TaskRunStatus.RUNNING, TaskRunStatus.FAILED, "my-task-name", taskId);

        // 2. Process
        mockEventPubSub.post(eventSuccess);
        mockEventPubSub.post(eventFailed);

        // 3. Validate
        ArgumentCaptor<Event> wecomServiceEventArgumentCaptor = ArgumentCaptor.forClass(Event.class);
        ArgumentCaptor<Event> emailServiceEventArgumentCaptor = ArgumentCaptor.forClass(Event.class);
        verify(weComService, times(1))
                .sendMessage(wecomServiceEventArgumentCaptor.capture());
        verify(emailService, times(1))
                .sendEmailByEventAndUserConfig(
                        emailServiceEventArgumentCaptor.capture(),
                        Mockito.eq(new EmailNotifierUserConfig(Collections.EMPTY_LIST, Collections.EMPTY_LIST))
                );
        assertThat(wecomServiceEventArgumentCaptor.getValue(), sameBeanAs(eventSuccess));
        assertThat(wecomServiceEventArgumentCaptor.getValue(), sameBeanAs(emailServiceEventArgumentCaptor.getValue()));
    }

    @Test
    public void workflowEventSubscriber_shouldNotifyFinishEventCorrectly() {
        // 1. Prepare
        long attemptId = 3L;
        long taskId = 1232L;

        // Expect to trigger both notifiers on success/failed event
        prepareTaskNotifyConfig(taskId, TaskStatusNotifyTrigger.ON_FINISH);
        TaskAttemptStatusChangeEvent eventSuccess = new TaskAttemptStatusChangeEvent(attemptId, TaskRunStatus.RUNNING, TaskRunStatus.SUCCESS, "my-task-name", taskId);
        TaskAttemptStatusChangeEvent eventFailed = new TaskAttemptStatusChangeEvent(attemptId, TaskRunStatus.RUNNING, TaskRunStatus.FAILED, "my-task-name", taskId);

        // 2. Process
        mockEventPubSub.post(eventSuccess);
        mockEventPubSub.post(eventFailed);

        // 3. Validate
        ArgumentCaptor<Event> wecomServiceEventArgumentCaptor = ArgumentCaptor.forClass(Event.class);
        ArgumentCaptor<Event> emailServiceEventArgumentCaptor = ArgumentCaptor.forClass(Event.class);
        verify(weComService, times(2))
                .sendMessage(wecomServiceEventArgumentCaptor.capture());
        verify(emailService, times(2))
                .sendEmailByEventAndUserConfig(
                        emailServiceEventArgumentCaptor.capture(),
                        Mockito.eq(new EmailNotifierUserConfig(Collections.EMPTY_LIST, Collections.EMPTY_LIST))
                );
        assertThat(wecomServiceEventArgumentCaptor.getAllValues().get(0), sameBeanAs(eventSuccess));
        assertThat(wecomServiceEventArgumentCaptor.getAllValues().get(1), sameBeanAs(eventFailed));
        assertThat(wecomServiceEventArgumentCaptor.getAllValues(), sameBeanAs(emailServiceEventArgumentCaptor.getAllValues()));
    }


    @Test
    public void workflowEventSubscriber_shouldNotNotifyEvent_whenTriggerTypeIsNever() {
        // 1. Prepare
        long attemptId = 4L;
        long taskId = 1233L;
        // Expect not to trigger any notifier
        prepareTaskNotifyConfig(taskId, TaskStatusNotifyTrigger.NEVER);
        TaskAttemptStatusChangeEvent eventSuccess = new TaskAttemptStatusChangeEvent(attemptId, TaskRunStatus.RUNNING, TaskRunStatus.SUCCESS, "my-task-name", taskId);
        TaskAttemptStatusChangeEvent eventFailed = new TaskAttemptStatusChangeEvent(attemptId, TaskRunStatus.RUNNING, TaskRunStatus.FAILED, "my-task-name", taskId);

        // 2. Mock by sending events
        mockEventPubSub.post(eventSuccess);
        mockEventPubSub.post(eventFailed);

        // 3. Validate
        verify(weComService, never()).sendMessage(any());
        verify(emailService, never()).sendEmailByEventAndUserConfig(any(), any());
    }

    @Test
    public void workflowEventDispatcher_shouldOnlyDispatchWecomNotifier_whenOnlyWeComNotifierEnabled() {
        // 1. Prepare
        long attemptId = 2L;
        long taskId = 1234L;

        // Should only trigger WeCom notifier, do not trigger email notifier
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
        mockEventPubSub.post(eventSuccess);
        mockEventPubSub.post(eventFailed);

        // 3. Validate
        ArgumentCaptor<Event> wecomServiceEventArgumentCaptor = ArgumentCaptor.forClass(Event.class);
        verify(weComService, times(2))
                .sendMessage(wecomServiceEventArgumentCaptor.capture());
        assertThat(wecomServiceEventArgumentCaptor.getAllValues().get(0), sameBeanAs(eventSuccess));
        assertThat(wecomServiceEventArgumentCaptor.getAllValues().get(1), sameBeanAs(eventFailed));

        verify(emailService, never()).sendEmailByEventAndUserConfig(any(), any());
    }

    @Test
    public void workflowEventDispatcher_shouldOnlyDispatchWecomNotifier_whenOnlyEmailNotifierEnabled() {
        // 1. Prepare
        long attemptId = 2L;
        long taskId = 1235L;

        // Should only trigger email notifier, do not trigger WeCom notifier
        EmailNotifierUserConfig presetEmailNotifierConfig = new EmailNotifierUserConfig(
                Lists.newArrayList("foo@bar.com"),
                Lists.newArrayList(1L, 2L, 3L)
        );
        taskNotifyConfigService.upsertTaskNotifyConfig(TaskNotifyConfig.newBuilder()
                .withWorkflowTaskId(taskId)
                .withTriggerType(TaskStatusNotifyTrigger.ON_FINISH)
                .withNotifierConfigs(Lists.newArrayList(presetEmailNotifierConfig))
                .build());
        TaskAttemptStatusChangeEvent eventSuccess = new TaskAttemptStatusChangeEvent(attemptId, TaskRunStatus.RUNNING, TaskRunStatus.SUCCESS, "my-task-name", taskId);
        TaskAttemptStatusChangeEvent eventFailed = new TaskAttemptStatusChangeEvent(attemptId, TaskRunStatus.RUNNING, TaskRunStatus.FAILED, "my-task-name", taskId);

        // 2. Process
        mockEventPubSub.post(eventSuccess);
        mockEventPubSub.post(eventFailed);

        // 3. Validate
        ArgumentCaptor<Event> emailServiceEventArgumentCaptor = ArgumentCaptor.forClass(Event.class);
        verify(emailService, times(2))
                .sendEmailByEventAndUserConfig(
                        emailServiceEventArgumentCaptor.capture(),
                        Mockito.eq(presetEmailNotifierConfig)
                );
        assertThat(emailServiceEventArgumentCaptor.getAllValues().get(0), sameBeanAs(eventSuccess));
        assertThat(emailServiceEventArgumentCaptor.getAllValues().get(1), sameBeanAs(eventFailed));

        verify(weComService, never()).sendMessage(any());
    }

    @Test
    public void workflowEventDispatcher_shouldUseSystemDefaultConfig_whenTaskIdExplicitlyRegistered() {
        // 1. Prepare
        long attemptId = 1L;
        long taskId = 1230L;

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
        mockEventPubSub.post(eventSuccess);
        mockEventPubSub.post(eventFailed);

        // 3. Validate
        ArgumentCaptor<Event> wecomServiceEventArgumentCaptor = ArgumentCaptor.forClass(Event.class);
        verify(weComService, times(1))
                .sendMessage(wecomServiceEventArgumentCaptor.capture());
        assertThat(wecomServiceEventArgumentCaptor.getValue(), sameBeanAs(eventFailed));

        verify(emailService, never()).sendEmailByEventAndUserConfig(any(), any());
    }

    @Test
    public void workflowEventDispatcher_shouldUseSystemDefaultConfig_whenTaskIdIsNotRegistered() {
        // 1. Prepare
        long attemptId = 1L;
        long taskId = 1240L;

        // Here we do not register notify configurations explicitly
        // But event dispatcher is still expected to notify by system default configuration

        TaskAttemptStatusChangeEvent eventFailed = new TaskAttemptStatusChangeEvent(attemptId, TaskRunStatus.RUNNING, TaskRunStatus.FAILED, "my-task-name", taskId);
        TaskAttemptStatusChangeEvent eventSuccess = new TaskAttemptStatusChangeEvent(attemptId, TaskRunStatus.RUNNING, TaskRunStatus.SUCCESS, "my-task-name-2", taskId);

        // 2. Process
        mockEventPubSub.post(eventSuccess);
        mockEventPubSub.post(eventFailed);

        // 3. Validate
        ArgumentCaptor<Event> wecomServiceEventArgumentCaptor = ArgumentCaptor.forClass(Event.class);
        verify(weComService, times(1))
                .sendMessage(wecomServiceEventArgumentCaptor.capture());
        assertThat(wecomServiceEventArgumentCaptor.getValue(), sameBeanAs(eventFailed));

        verify(emailService, never()).sendEmailByEventAndUserConfig(any(), any());
    }

    @Test
    public void workflowEventSubscriber_shouldNotNotifyWhenTaskNameMatched() {
        // 1. Prepare
        long attemptId = 1L;
        long taskId = 1241L;

        // Should not trigger, taskName matched DataQuality Task
        prepareTaskNotifyConfig(taskId, TaskStatusNotifyTrigger.ON_FAIL);
        TaskAttemptStatusChangeEvent eventSuccess = new TaskAttemptStatusChangeEvent(attemptId, TaskRunStatus.RUNNING, TaskRunStatus.SUCCESS, DataQualityConstant.WORKFLOW_TASK_NAME_PREFIX, taskId);
        TaskAttemptStatusChangeEvent eventFailed = new TaskAttemptStatusChangeEvent(attemptId, TaskRunStatus.RUNNING, TaskRunStatus.FAILED, DataQualityConstant.WORKFLOW_TASK_NAME_PREFIX, taskId);

        // 2. Process
        mockEventPubSub.post(eventSuccess);
        mockEventPubSub.post(eventFailed);

        // 3. Validate
        verify(weComService, never()).sendMessage(any());
        verify(emailService, never()).sendEmailByEventAndUserConfig(any(), any());
    }

    @Test
    public void workflowEventSubscriber_shouldNotNotifyForBackfillTask() {
        // 1. Prepare
        long attemptId = 1L;
        long taskId = 1242L;

        // Should not trigger for backfill task
        prepareTaskNotifyConfig(taskId, TaskStatusNotifyTrigger.ON_FAIL);
        TaskAttemptStatusChangeEvent eventSuccess = new TaskAttemptStatusChangeEvent(attemptId, TaskRunStatus.RUNNING, TaskRunStatus.SUCCESS, "my-task-name", taskId);
        TaskAttemptStatusChangeEvent eventFailed = new TaskAttemptStatusChangeEvent(attemptId, TaskRunStatus.RUNNING, TaskRunStatus.FAILED, "my-task-name", taskId);
        prepareBackfillTaskRunRelation(eventSuccess.getTaskRunId());

        // 2. Process
        mockEventPubSub.post(eventSuccess);
        mockEventPubSub.post(eventFailed);

        // 3. Validate
        verify(weComService, never()).sendMessage(any());
        verify(emailService, never()).sendEmailByEventAndUserConfig(any(), any());
    }

    @Test
    public void workflowEventSubscriber_shouldNotNotifyForDryRun() {
        // 1. Prepare
        long attemptId = 1L;
        long taskId = 1243L;

        // Should not trigger for dryRun task
        prepareTaskNotifyConfig(taskId, TaskStatusNotifyTrigger.ON_FAIL);
        TaskAttemptStatusChangeEvent eventSuccess = new TaskAttemptStatusChangeEvent(attemptId, TaskRunStatus.RUNNING, TaskRunStatus.SUCCESS, "my-task-name", taskId);
        TaskAttemptStatusChangeEvent eventFailed = new TaskAttemptStatusChangeEvent(attemptId, TaskRunStatus.RUNNING, TaskRunStatus.FAILED, "my-task-name", taskId);
        prepareTaskTry(eventSuccess.getTaskRunId());

        // 2. Process
        mockEventPubSub.post(eventSuccess);
        mockEventPubSub.post(eventFailed);

        // 3. Validate
        verify(weComService, never()).sendMessage(any());
        verify(emailService, never()).sendEmailByEventAndUserConfig(any(), any());
    }

    private void prepareBackfillTaskRunRelation(Long taskRunId) {
        Backfill backfill = MockBackfillFactory.createBackfill(taskRunId);
        backfillDao.create(backfill);
    }

    private void prepareTaskTry(Long taskRunId) {
        TaskTry taskTry = MockTaskDefinitionFactory.createTaskTry(taskRunId);
        taskTryDao.create(taskTry);
    }

}
