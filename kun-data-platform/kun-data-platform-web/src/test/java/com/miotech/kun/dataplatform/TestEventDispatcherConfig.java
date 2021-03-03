package com.miotech.kun.dataplatform;

import com.miotech.kun.dataplatform.notify.MockWorkflowEventPubSubListener;
import com.miotech.kun.dataplatform.notify.WorkflowEventDispatcher;
import com.miotech.kun.dataplatform.notify.service.EmailService;
import com.miotech.kun.dataplatform.notify.service.ZhongdaService;
import com.miotech.kun.dataplatform.notify.userconfig.EmailNotifierUserConfig;
import com.miotech.kun.workflow.core.event.Event;
import com.miotech.kun.workflow.core.event.EventReceiver;
import com.miotech.kun.workflow.core.event.TaskAttemptStatusChangeEvent;
import com.miotech.kun.workflow.core.publish.EventSubscriber;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TestEventDispatcherConfig {
    private static final EmailService mockEmailService = Mockito.mock(EmailService.class);

    private static final ZhongdaService mockZhongdaService = Mockito.mock(ZhongdaService.class);

    private static final EventSubscriber mockEventSubscriber = Mockito.mock(EventSubscriber.class);

    private MockWorkflowEventPubSubListener eventPubSubListener = null;

    private volatile int emailServiceNotifyInvokeCount = 0;

    private volatile int zhongdaServiceNotifyInvokeCount = 0;

    @Bean
    public EmailService createMockEmailService() {
        Mockito.doAnswer(invocation -> {
                    emailServiceNotifyInvokeCount += 1;
                    return null;
                })
                .when(mockEmailService)
                .sendEmailByEventAndUserConfig(Mockito.isA(Event.class), Mockito.isA(EmailNotifierUserConfig.class));
        return mockEmailService;
    }

    @Bean
    public ZhongdaService createMockZhongdaService() {
        Mockito.doAnswer(invocation -> {
            zhongdaServiceNotifyInvokeCount += 1;
            return null;
        })
                .when(mockZhongdaService)
                .sendMessage(Mockito.isA(TaskAttemptStatusChangeEvent.class));
        return mockZhongdaService;
    }

    @Bean
    public EventSubscriber createMockEventSubscriber() {
        Mockito.doAnswer(invocation -> {
            eventPubSubListener = new MockWorkflowEventPubSubListener(invocation.getArgument(0));
            return null;
        }).when(mockEventSubscriber).subscribe(Mockito.isA(EventReceiver.class));

        return mockEventSubscriber;
    }

    @Bean
    public WorkflowEventDispatcher createTestWorkflowEventDispatcher() {
        return new WorkflowEventDispatcher();
    }

    public MockWorkflowEventPubSubListener getEventPubSubListener() {
        return eventPubSubListener;
    }

    public int getEmailServiceNotifyInvokeCount() {
        return emailServiceNotifyInvokeCount;
    }

    public int getZhongdaServiceNotifyInvokeCount() {
        return zhongdaServiceNotifyInvokeCount;
    }
}
