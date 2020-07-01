package com.miotech.kun.commons.web.module;

import com.google.inject.Provides;
import com.google.inject.Singleton;
import org.eclipse.jetty.server.Server;

import java.util.Properties;

public class KunWebServerModule extends AppModule {

    public KunWebServerModule(Properties props) {
        super(props);
    }

    @Provides
    @Singleton
    public Server getJettyServer() {
        return new Server();
    }
}
