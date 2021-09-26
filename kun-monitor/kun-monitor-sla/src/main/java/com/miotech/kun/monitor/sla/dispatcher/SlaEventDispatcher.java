package com.miotech.kun.monitor.sla.dispatcher;

import com.miotech.kun.commons.pubsub.subscribe.EventSubscriber;
import com.miotech.kun.monitor.sla.common.service.TaskTimelineService;
import com.miotech.kun.workflow.core.event.TaskRunCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Slf4j
@Component
public class SlaEventDispatcher {

    @Autowired
    @Qualifier("sla-subscriber")
    private EventSubscriber workflowEventSubscriber;

    @Autowired
    private TaskTimelineService taskTimelineService;

    @PostConstruct
    private void onDispatcherConstructed() {
        doSubscribe();
    }

    private void doSubscribe() {
        workflowEventSubscriber.subscribe(event -> {
            if (event instanceof TaskRunCreatedEvent) {
                handleTaskRunCreatedEvent((TaskRunCreatedEvent) event);
            }
        });
    }

    private void handleTaskRunCreatedEvent(TaskRunCreatedEvent event) {
        taskTimelineService.handleTaskRunCreatedEvent(event);
    }

}
