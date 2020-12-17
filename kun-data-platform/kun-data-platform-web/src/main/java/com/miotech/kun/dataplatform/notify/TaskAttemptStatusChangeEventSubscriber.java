package com.miotech.kun.dataplatform.notify;

import com.miotech.kun.workflow.core.event.Event;
import com.miotech.kun.workflow.core.event.EventReceiver;
import com.miotech.kun.workflow.core.publish.EventSubscriber;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;

public class TaskAttemptStatusChangeEventSubscriber {

    @Autowired
    private EventSubscriber subscriber;

    @Autowired
    private MessageNotifier wechatNotifier;

    @PostConstruct
    private void doSubscribe(){
        this.subscribe();
    }

    public void subscribe(){
        EventReceiver receiver = new TaskAttemptStatusChangeEventReceiver();
        subscriber.subscribe(receiver);
    }

    private class TaskAttemptStatusChangeEventReceiver implements EventReceiver {
        @Override
        public void onReceive(Event event) {
            if(event != null)
                wechatNotifier.notify(event);
        }
    }
}
