package com.miotech.kun.workflow.web;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.internal.AbstractBindingBuilder;
import com.miotech.kun.workflow.common.AppModule;
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
