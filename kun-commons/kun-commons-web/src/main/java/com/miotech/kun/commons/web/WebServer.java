package com.miotech.kun.commons.web;

import com.google.inject.Injector;

public interface WebServer {

    void start();

    void shutdown();

    String getState();

    boolean isReady();

    void init(Injector injector);

}
