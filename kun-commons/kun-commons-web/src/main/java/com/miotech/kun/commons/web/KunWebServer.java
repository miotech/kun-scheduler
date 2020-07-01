package com.miotech.kun.commons.web;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.miotech.kun.commons.web.constant.ConfigurationKeys;
import com.miotech.kun.commons.web.handler.DispatchServlet;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

@Singleton
public class KunWebServer {
    private static final Logger logger = LoggerFactory.getLogger(KunWebServer.class);

    private final Server server;
    private final DispatchServlet dispatchServlet;
    private final Properties props;

    @Inject
    public KunWebServer(final Properties props,
                        final Server server,
                        final DispatchServlet dispatchServlet) {
        this.server = server;
        this.dispatchServlet = dispatchServlet;
        this.props = props;
    }

    private void configureServlet() {
        final ServletContextHandler root = new ServletContextHandler(this.server, "/", ServletContextHandler.SESSIONS);
        final ServletHolder dispatchServlet = new ServletHolder(this.dispatchServlet);
        root.addServlet(dispatchServlet, "/*");
    }

    public void start() {
        try {
            configureServlet();
            ServerConnector connector = new ServerConnector(server);
            int port = Integer.parseInt(props.getProperty(ConfigurationKeys.PROP_SERVER_PORT, "8088"));
            boolean enableStdErr = props.getProperty(ConfigurationKeys.PROP_SERVER_DUMP_STDERR, "false").equals("true");

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

    public void shutdown() {
        try {
            logger.info("Prepare to shutdown server");
            this.server.stop();
        } catch (Exception e) {
            logger.error("", e);
        }
    }

    public boolean isServerRunning() {
        return this.server.isRunning();
    }

}
