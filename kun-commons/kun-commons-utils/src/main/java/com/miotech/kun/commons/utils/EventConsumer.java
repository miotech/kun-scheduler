package com.miotech.kun.commons.utils;

public abstract class EventConsumer<T, E> implements EventHandler<E> {
    private EventLoop<T, E> eventLoop;

    /**
     * Default callback method for event handling.
     * @param event
     */
    public abstract void onReceive(E event);

    protected void listenTo(T key, EventHandler<E> handler) {
        eventLoop.register(key, handler);
    }

    protected void unlistenTo(T key) {
        eventLoop.unregister(key);
    }

    void setEventLoop(EventLoop<T, E> eventLoop) {
        this.eventLoop = eventLoop;
    }
}
