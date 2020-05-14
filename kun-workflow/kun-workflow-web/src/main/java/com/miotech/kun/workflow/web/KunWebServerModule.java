package com.miotech.kun.workflow.web;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import org.eclipse.jetty.server.Server;

public class KunWebServerModule extends AbstractModule {

    @Provides
    @Singleton
    public Server getJettyServer() {
        return new Server();
    }
}
