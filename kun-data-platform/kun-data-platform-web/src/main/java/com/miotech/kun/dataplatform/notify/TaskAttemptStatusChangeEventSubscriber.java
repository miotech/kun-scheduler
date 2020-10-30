package com.miotech.kun.dataplatform.notify;

import com.google.inject.Inject;
import com.miotech.kun.workflow.core.event.Event;
import com.miotech.kun.workflow.core.event.EventReceiver;
import com.miotech.kun.workflow.core.event.LineageEvent;
import com.miotech.kun.workflow.core.event.TaskAttemptStatusChangeEvent;
import com.miotech.kun.workflow.core.publish.EventSubscriber;
import com.miotech.kun.workflow.core.publish.KafkaEventSubscriber;
import org.springframework.beans.factory.annotation.Autowired;

public class TaskAttemptStatusChangeEventSubscriber {

    @Autowired
    private KafkaEventSubscriber subscriber;

    @Autowired
    private WechatNotifier wechatNotifier;

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
