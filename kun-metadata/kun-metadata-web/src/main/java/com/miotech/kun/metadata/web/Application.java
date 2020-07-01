package com.miotech.kun.metadata.web;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.miotech.kun.commons.web.KunWebServer;
import com.miotech.kun.commons.web.module.CommonModule;
import com.miotech.kun.commons.web.module.KunWebServerModule;
import com.miotech.kun.workflow.common.constant.ConfigurationKeys;
import com.miotech.kun.workflow.db.DatabaseSetup;
import com.miotech.kun.workflow.utils.PropertyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.util.Properties;

@Singleton
public class Application {
    private static final Logger logger = LoggerFactory.getLogger(Application.class);

    private final Properties props;
    private final DataSource dataSource;

    @Inject
    public Application(Properties props, DataSource dataSource) {
        this.props = props;
        this.dataSource = dataSource;
    }

    public static void main(String[] args) {
        logger.info("Starting Jetty Kun Web Server...");

        /* Initialize Guice Injector */
        Properties props = PropertyUtils.loadAppProps();
        final Injector injector = Guice.createInjector(
                new KunWebServerModule(props),
                new CommonModule(),
                new PackageScanModule()
        );

        injector.getInstance(Application.class).configureDB();
        injector.getInstance(KunWebServer.class).start();
    }

    private void configureDB() {
        String migrationDir = props.getProperty(ConfigurationKeys.PROP_FLYWAY_MIRGRATION, "sql/");
        String schemaHistory = props.getProperty(ConfigurationKeys.PROP_FLYWAY_TABLENAME, DatabaseSetup.DEFAULT_SCHEMA_HISTORY_TABLE);
        DatabaseSetup setup = new DatabaseSetup(schemaHistory, dataSource, migrationDir);
        setup.start();
    }

}
