package com.miotech.kun.workflow.web;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.miotech.kun.common.CommonModule;
import com.miotech.kun.workflow.utils.PropertyUtils;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

@Singleton
public class KunWebServer {
    private static final Logger logger = LoggerFactory.getLogger(KunWebServer.class);

    private final Server server;
    private final DispatchServlet dispatchServlet;

    @Inject
    public KunWebServer(final Server server, DispatchServlet dispatchServlet) {
        this.server = server;
        this.dispatchServlet = dispatchServlet;
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
        Properties props = PropertyUtils.loadAppProps();
        final Injector injector = Guice.createInjector(
                new KunWebServerModule(props),
                new CommonModule()
        );

        // If in dev mode, initialize in-memory database
        if (props.containsKey("devDB.init")) {
            String initSqlFile = (String) props.get("devDB.init");
            logger.debug("Initializing DEV DB with SQL file: {}", initSqlFile);
            KunWebServer.createDevTables(injector, initSqlFile);
        }

        launch(injector.getInstance(KunWebServer.class));
    }

    public static void createDevTables(Injector injector, String initSqlFile) {
        DataSource dataSource = injector.getInstance(DataSource.class);

        try (Connection conn = dataSource.getConnection()) {
            InputStream inputStream = KunWebServer.class
                    .getClassLoader()
                    .getResourceAsStream(initSqlFile);

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String createDDL = "";
            String line;
            while((line = reader.readLine())!=null) {
                createDDL += line;
            }

            String createJsonbDomain = "CREATE DOMAIN IF NOT EXISTS \"JSONB\" AS TEXT;";
            conn.createStatement().execute(createJsonbDomain);
            conn.createStatement().execute(createDDL);
        } catch (SQLException | IOException e) {
            logger.error("Failed to establish connection.", e);
            throw new RuntimeException(e);
        }
    }

    public static void launch(final KunWebServer webServer) throws Exception {
        /* This creates the Web Server instance */
        webServer.start();
    }
}
