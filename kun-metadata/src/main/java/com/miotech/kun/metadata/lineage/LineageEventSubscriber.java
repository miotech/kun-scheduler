package com.miotech.kun.metadata.lineage;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.miotech.kun.workflow.core.event.Event;
import com.miotech.kun.workflow.core.event.EventReceiver;
import com.miotech.kun.workflow.core.event.LineageEvent;
import com.miotech.kun.workflow.core.publish.EventSubscriber;
import com.miotech.kun.workflow.core.publish.KafkaEventSubscriber;

import javax.inject.Named;

@Singleton
public class LineageEventSubscriber {
    private EventSubscriber subscriber;

    @Named("kafka.topic")
    private String topic;

    @Inject
    private LineageLoader loader;

    public LineageEventSubscriber(){
        this.topic = topic;
        subscriber = new KafkaEventSubscriber(topic, props);
    }

    public void subscribe(){
        EventReceiver receiver = new LineageEventReceiver();
        subscriber.subscribe(receiver);
    }

    private class LineageEventReceiver implements EventReceiver {
        @Override
        public void onReceive(Event event) {
            LineageEvent lineageEvent = (LineageEvent) event;
            loader.saveToDB(lineageEvent);
        }
    }
}
