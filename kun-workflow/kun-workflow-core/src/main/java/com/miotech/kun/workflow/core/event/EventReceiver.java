package com.miotech.kun.workflow.core.event;

public interface EventReceiver {
    /**
     * 向EventReceiver投递消息。
     * @param event
     */
    public void post(Event event);
}
