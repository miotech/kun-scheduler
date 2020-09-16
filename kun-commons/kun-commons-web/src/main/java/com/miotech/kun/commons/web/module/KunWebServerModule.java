package com.miotech.kun.commons.web.module;

import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.miotech.kun.commons.utils.Props;
import org.eclipse.jetty.server.Server;


public class KunWebServerModule extends AppModule {

    public KunWebServerModule(Props props) {
        super(props);
    }

    @Provides
    @Singleton
    public Server getJettyServer() {
        return new Server();
    }
}
