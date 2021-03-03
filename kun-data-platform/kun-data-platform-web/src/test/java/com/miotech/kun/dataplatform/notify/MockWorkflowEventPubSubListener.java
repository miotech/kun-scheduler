package com.miotech.kun.dataplatform.notify;

import com.miotech.kun.workflow.core.event.Event;
import com.miotech.kun.workflow.core.event.EventReceiver;

public class MockWorkflowEventPubSubListener {
    private final EventReceiver eventReceiver;

    public MockWorkflowEventPubSubListener(EventReceiver eventReceiver) {
        this.eventReceiver = eventReceiver;
    }

    public EventReceiver getEventReceiver() {
        return this.eventReceiver;
    }

    public void mockReceiveEventFromWorkflow(Event event) {
        this.eventReceiver.onReceive(event);
    }
}
