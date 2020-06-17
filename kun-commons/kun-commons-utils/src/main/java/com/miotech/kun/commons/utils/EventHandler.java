package com.miotech.kun.commons.utils;

@FunctionalInterface
public interface EventHandler<E> {
    public void onReceive(E event);
}
