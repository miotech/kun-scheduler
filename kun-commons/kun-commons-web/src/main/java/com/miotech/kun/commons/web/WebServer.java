package com.miotech.kun.commons.web;

public interface WebServer {

    void start();

    void shutdown();

    String getState();

    boolean isReady();

}
