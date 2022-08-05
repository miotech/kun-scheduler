package com.miotech.kun.datadiscovery.event;

import com.miotech.kun.commons.pubsub.subscribe.EventSubscriber;
import com.miotech.kun.datadiscovery.service.RdmService;
import com.miotech.kun.metadata.core.model.event.DatasetCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
@Slf4j
public class MetaDataEventSubscriber {

    @Autowired
    @Qualifier("data-discovery-subscriber")
    private EventSubscriber eventSubscriber;

    @Autowired
    private RdmService rdmService;


    @PostConstruct
    private void onDispatcherConstructed() {
        doSubscribe();
    }

    private void doSubscribe() {
        eventSubscriber.subscribe(event -> {
            log.debug("accept dataset event:{}", event);
            if (event instanceof DatasetCreatedEvent) {
                rdmService.linkage((DatasetCreatedEvent) event);
            }

        });
    }


}
