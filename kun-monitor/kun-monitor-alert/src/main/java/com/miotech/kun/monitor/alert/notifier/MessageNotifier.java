package com.miotech.kun.monitor.alert.notifier;

import com.miotech.kun.commons.pubsub.event.Event;

public interface MessageNotifier {

    void notify(Long workflowTaskId, String subject, String message);

    /**
     * Notify a event through corresponding nofication service.
     * @param event a workflow event object
     */
    void notifyTaskStatusChange(Event event);
}
