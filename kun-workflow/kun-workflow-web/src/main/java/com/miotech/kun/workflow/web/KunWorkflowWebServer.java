package com.miotech.kun.workflow.web;

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
public class KunWorkflowWebServer extends AbstractKunWebServer {
    private static final Logger logger = LoggerFactory.getLogger(KunWorkflowWebServer.class);

    @Inject
    public KunWorkflowWebServer(Injector injector) {
        super(injector);
    }

    public static void main(final String[] args) {
        logger.info("Starting Jetty Kun Web Server...");

        /* Load Props */
        Props props = PropsUtils.loadAppProps();
        // Redirect all std out and err messages into log4j
        org.eclipse.jetty.util.log.Log.setLog(new JettyLog());

        /* Initialize Guice Injector */
        final Injector injector = Guice.createInjector(new KunWorkflowServerModule(props));
        WebServer webServer = injector.getInstance(KunWorkflowWebServer.class);
        // Start
        webServer.start();
    }

}
