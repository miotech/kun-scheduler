package com.miotech.kun.commons.web;

import com.google.inject.Injector;
import com.miotech.kun.commons.utils.Initializer;
import com.miotech.kun.commons.utils.Props;
import com.miotech.kun.commons.web.constant.ConfigurationKeys;
import com.miotech.kun.commons.web.handler.DispatchServlet;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractKunWebServer implements WebServer {
    private static final Logger logger = LoggerFactory.getLogger(AbstractKunWebServer.class);

    private final Server server;
    private final DispatchServlet dispatchServlet;
    private final Props props;
    private final Injector injector;

    public AbstractKunWebServer(Injector injector) {
        this.injector = injector;
        this.server = injector.getInstance(Server.class);
        this.dispatchServlet = injector.getInstance(DispatchServlet.class);
        this.props = injector.getInstance(Props.class);
    }

    @Override
    public final void start() {
        init(injector);
        startWebServer();
    }

    @Override
    public final void init(Injector injector) {
        Initializer initializer = injector.getInstance(Initializer.class);
        initializer.initialize();
    }

    @Override
    public final void shutdown() {
        try {
            logger.info("Prepare to shutdown server");
            this.server.stop();
        } catch (Exception e) {
            logger.error("", e);
        }
    }

    @Override
    public final String getState() {
        return this.server.getState();
    }

    @Override
    public final boolean isReady() {
        return this.server.isStarted();
    }

    private void startWebServer() {
        try {
            configureServlet();
            ServerConnector connector = new ServerConnector(server);
            int port = props.getInt(ConfigurationKeys.PROP_SERVER_PORT, 8088);
            boolean enableStdErr = props.getBoolean(ConfigurationKeys.PROP_SERVER_DUMP_STDERR, false);

            logger.info("Server listen on: {}", port);
            connector.setPort(port);
            this.server.addConnector(connector);
            this.server.start();
            if (enableStdErr) {
                this.server.dumpStdErr();
            }
            this.server.join();
        } catch (final Exception e) {
            logger.error("", e);
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }

    private void configureServlet() {
        final ServletContextHandler root = new ServletContextHandler(this.server, "/", ServletContextHandler.SESSIONS);
        final ServletHolder dispatchServlet = new ServletHolder(this.dispatchServlet);
        root.addServlet(dispatchServlet, "/*");
    }

}
