package com.miotech.kun.monitor.sla.dispatcher;

import com.fasterxml.jackson.core.type.TypeReference;
import com.miotech.kun.commons.pubsub.event.Event;
import com.miotech.kun.commons.pubsub.event.EventReceiver;
import com.miotech.kun.commons.pubsub.event.PrivateEvent;
import com.miotech.kun.commons.pubsub.subscribe.EventSubscriber;
import com.miotech.kun.commons.utils.IdGenerator;
import com.miotech.kun.monitor.sla.AppTestBase;
import com.miotech.kun.monitor.sla.common.service.TaskTimelineService;
import com.miotech.kun.monitor.facade.model.alert.SystemDefaultNotifierConfig;
import com.miotech.kun.monitor.facade.model.alert.NotifierUserConfig;
import com.miotech.kun.monitor.facade.model.alert.TaskStatusNotifyTrigger;
import com.miotech.kun.workflow.core.event.TaskRunCreatedEvent;
import com.miotech.kun.workflow.utils.JSONUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Import(SlaEventDispatcherTest.SlaEventDispatcherTestConfig.class)
public class SlaEventDispatcherTest extends AppTestBase {

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
    public static class SlaEventDispatcherTestConfig {
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

        @Bean("sla-subscriber")
        @Primary
        public EventSubscriber createMockEventSubscriber() {
            return new MockEventPubSub();
        }

        @Bean
        @Primary
        public SlaEventDispatcher createWorkflowEventDispatcher() {
            return new SlaEventDispatcher();
        }
    }

    @Autowired
    private EventSubscriber eventSubscriber;

    @SpyBean
    private TaskTimelineService taskTimelineService;

    private MockEventPubSub mockEventPubSub;

    @Before
    public void beforeTest() {
        mockEventPubSub = (MockEventPubSub) eventSubscriber;
    }

    @Test
    public void testDoSubscribe_RandomMockEvent() {
        Event nonRelatedEvent = new RandomMockEvent();
        mockEventPubSub.post(nonRelatedEvent);
        verify(taskTimelineService, never()).handleTaskRunCreatedEvent(any());
    }

    @Test
    public void testDoSubscribe_TaskRunCreatedEvent() {
        TaskRunCreatedEvent taskRunCreatedEvent = new TaskRunCreatedEvent(IdGenerator.getInstance().nextId(), IdGenerator.getInstance().nextId());
        mockEventPubSub.post(taskRunCreatedEvent);

        verify(taskTimelineService, times(1)).handleTaskRunCreatedEvent(any());
    }

    /**
     * A dummy mock event type to test response of listener (expect to take no effect)
     */
    private static class RandomMockEvent extends PrivateEvent {
    }

}
