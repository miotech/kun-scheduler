package com.miotech.kun.workflow.core.event;

public interface EventReceiver<E extends Event> {
    /**
     * 向EventReceiver投递消息。
     * @param event
     */
    public default void post(E event) {
        onReceive(event);
    }

    /**
     * 收到消息时的回调函数。
     * @param event
     */
    public void onReceive(E event);
}
