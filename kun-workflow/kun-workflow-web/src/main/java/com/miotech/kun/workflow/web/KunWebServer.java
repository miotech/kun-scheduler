package com.miotech.kun.workflow.web;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.miotech.kun.workflow.web.ServerContext.SERVER_CONTEXT;

@Singleton
public class KunWebServer {
    private static final Logger logger = LoggerFactory.getLogger(KunWebServer.class);

    private final Server server;

    @Inject
    public KunWebServer(final Server server) {
        this.server = server;
    }

    private void configureServlet() {
        final ServletContextHandler root = new ServletContextHandler(this.server, "/", ServletContextHandler.SESSIONS);
        final ServletHolder dispatchServlet = new ServletHolder(new DispatchServlet());
        root.addServlet(dispatchServlet, "/*");
    }

    public void start() {
        try {
            configureServlet();
            ServerConnector connector = new ServerConnector(server);
            connector.setPort(8088);
            this.server.addConnector(connector);
            this.server.start();
            this.server.dumpStdErr();
            this.server.join();
        } catch (final Exception e) {
            logger.error("{}", e);
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }

    public static void main(final String[] args) throws Exception {
        // Redirect all std out and err messages into log4j

        logger.info("Starting Jetty Kun Web Server...");

        /* Initialize Guice Injector */
        final Injector injector = Guice.createInjector(
                new KunWebServerModule()
        );

        SERVER_CONTEXT.setInjector(injector);
        launch(injector.getInstance(KunWebServer.class));
    }

    public static void launch(final KunWebServer webServer) throws Exception {
        /* This creates the Web Server instance */
        webServer.start();
    }
}
