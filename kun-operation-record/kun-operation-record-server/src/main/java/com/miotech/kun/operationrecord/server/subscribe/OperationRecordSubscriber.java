package com.miotech.kun.operationrecord.server.subscribe;

import com.miotech.kun.commons.pubsub.subscribe.EventSubscriber;
import com.miotech.kun.operationrecord.common.event.OperationRecordEvent;
import com.miotech.kun.operationrecord.server.service.OperationRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
@Slf4j
public class OperationRecordSubscriber {

    @Autowired
    @Qualifier("operationRecord-subscriber")
    private EventSubscriber operationRecordSubscriber;

    @Autowired
    private OperationRecordService operationRecordService;

    @PostConstruct
    private void onDispatcherConstructed() {
        doSubscribe();
    }

    private void doSubscribe() {
        operationRecordSubscriber.subscribe(event -> {
            if (event instanceof OperationRecordEvent) {
                handleBaseOperationEvent((OperationRecordEvent) event);
            }
        });
    }

    private void handleBaseOperationEvent(OperationRecordEvent event) {
        operationRecordService.create(event);
    }

}
