package com.miotech.kun.metadata.web.lineage;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.miotech.kun.workflow.core.event.Event;
import com.miotech.kun.workflow.core.event.EventReceiver;
import com.miotech.kun.workflow.core.event.LineageEvent;
import com.miotech.kun.workflow.core.publish.EventSubscriber;

@Singleton
public class LineageEventSubscriber {

    @Inject
    private EventSubscriber subscriber;

    @Inject
    private LineageLoader loader;

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
