package com.miotech.kun.workflow.core.event;

public interface EventReceiver {
    /**
     * 向EventReceiver投递消息。
     * @param event
     */
    public default void post(Event event) {
        onReceive(event);
    }

    /**
     * 收到消息时的回调函数。
     * @param event
     */
    public void onReceive(Event event);
}
