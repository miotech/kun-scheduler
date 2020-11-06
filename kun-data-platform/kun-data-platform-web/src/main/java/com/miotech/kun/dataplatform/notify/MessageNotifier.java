package com.miotech.kun.dataplatform.notify;

import com.miotech.kun.workflow.core.event.Event;

public interface MessageNotifier {

    public void notify(Event event);
}
