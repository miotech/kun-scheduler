package com.miotech.kun.workflow.web;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
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

    @Provides
    @Singleton
    public ObjectMapper getJsonObjectMapper() {

        ObjectMapper objectMapper = new ObjectMapper();
        // allow empty field
        objectMapper.setVisibility(PropertyAccessor.FIELD,
                JsonAutoDetect.Visibility.ANY);

        // default serialize datetime as iso date
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.setDateFormat(new StdDateFormat().withColonInTimeZone(true));

        return objectMapper;
    }
}
