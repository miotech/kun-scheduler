package com.miotech.kun.metadata.web;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.miotech.kun.commons.utils.Props;
import com.miotech.kun.commons.utils.PropsUtils;
import com.miotech.kun.commons.web.AbstractKunWebServer;
import com.miotech.kun.commons.web.WebServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class Application extends AbstractKunWebServer {
    private static final Logger logger = LoggerFactory.getLogger(Application.class);

    @Inject
    public Application(Injector injector) {
        super(injector);
    }

    public static void main(String[] args) {
        logger.info("Starting Jetty Kun Web Server...");

        /* Load Props */
        Props props = PropsUtils.loadAppProps();

        /* Initialize Guice Injector */
        final Injector injector = Guice.createInjector(new KunMetadataModule(props));
        WebServer webServer = injector.getInstance(Application.class);

        // Start
        webServer.start();
    }

}
