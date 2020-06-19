package com.miotech.kun.workflow.web;

import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.miotech.kun.workflow.common.AppModule;
import com.miotech.kun.workflow.core.Executor;
import com.miotech.kun.workflow.executor.local.LocalExecutor;
import org.eclipse.jetty.server.Server;

import java.util.Properties;

public class KunWebServerModule extends AppModule {

    public KunWebServerModule(Properties props) {
        super(props);
    }

    @Override
    protected void configure() {
        super.configure();
        bind(Executor.class).to(LocalExecutor.class);
    }

    @Provides
    @Singleton
    public Server getJettyServer() {
        return new Server();
    }
}
