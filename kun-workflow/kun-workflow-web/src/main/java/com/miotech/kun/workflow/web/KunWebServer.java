package com.miotech.kun.workflow.web;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.miotech.kun.workflow.SchedulerManager;
import com.miotech.kun.workflow.SchedulerModule;
import com.miotech.kun.workflow.common.CommonModule;
import com.miotech.kun.workflow.common.constant.ConfigurationKeys;
import com.miotech.kun.commons.db.DatabaseSetup;
import com.miotech.kun.commons.utils.PropertyUtils;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.util.Properties;

@Singleton
public class KunWebServer {
    private static final Logger logger = LoggerFactory.getLogger(KunWebServer.class);

    private final Server server;
    private final DispatchServlet dispatchServlet;
    private final Properties props;
    private final DataSource dataSource;
    private final SchedulerManager schedulerManager;

    @Inject
    public KunWebServer(final Properties props,
                        final DataSource dataSource,
                        final SchedulerManager schedulerManager,
                        final Server server,
                        final DispatchServlet dispatchServlet) {
        this.server = server;
        this.dispatchServlet = dispatchServlet;
        this.props = props;
        this.dataSource = dataSource;
        this.schedulerManager = schedulerManager;
    }

    private void configureServlet() {
        final ServletContextHandler root = new ServletContextHandler(this.server, "/", ServletContextHandler.SESSIONS);
        final ServletHolder dispatchServlet = new ServletHolder(this.dispatchServlet);
        root.addServlet(dispatchServlet, "/*");
    }

    private void configureDB() {
        String migrationDir = props.getProperty(ConfigurationKeys.PROP_FLYWAY_MIRGRATION, "sql/");
        String schemaHistory = props.getProperty(ConfigurationKeys.PROP_FLYWAY_TABLENAME, DatabaseSetup.DEFAULT_SCHEMA_HISTORY_TABLE);
        DatabaseSetup setup = new DatabaseSetup(schemaHistory, dataSource, migrationDir);
        setup.start();
    }

    public void start() {
        try {
            configureDB();
            schedulerManager.start();
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

    public static void main(final String[] args) {
        // Redirect all std out and err messages into log4j

        logger.info("Starting Jetty Kun Web Server...");

        /* Initialize Guice Injector */
        Properties props = PropertyUtils.loadAppProps();
        final Injector injector = Guice.createInjector(
                new KunWebServerModule(props),
                new CommonModule(),
                new SchedulerModule()
        );

        launch(injector.getInstance(KunWebServer.class));
    }

    private static void launch(final KunWebServer webServer) {
        /* This creates the Web Server instance */
        webServer.start();
    }
}
