package com.miotech.kun.dataplatform.notify;

import com.miotech.kun.workflow.core.event.Event;

public interface MessageNotifier {
    /**
     * Notify a event through corresponding nofication service.
     * @param event a workflow event object
     */
    public void notify(Event event);
}
