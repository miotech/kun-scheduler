package com.miotech.kun.workflow.db;

import com.zaxxer.hikari.HikariDataSource;
import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.util.concurrent.atomic.AtomicBoolean;
import java.sql.Connection;
import java.sql.SQLException;
public class DatabaseSetup {
    private final Logger logger = LoggerFactory.getLogger(DatabaseSetup.class);

    public static final String DEFAULT_SCHEMA_HISTORY_TABLE = "flyway_schema_history";
    private final DataSource dataSource;
    private final String[] locations;
    private final AtomicBoolean initialized;
    private final String tableName;


    public DatabaseSetup(DataSource dataSource, String... locations) {
        this(DEFAULT_SCHEMA_HISTORY_TABLE, dataSource, locations);
    }

    public DatabaseSetup(String tableName, DataSource dataSource, String... locations) {
        this.dataSource = dataSource;
        this.tableName = tableName;
        this.locations = locations;
        this.initialized = new AtomicBoolean(false);
    }

    public void start() {
        if (initialized.compareAndSet(false, true)) {
            String driverClass = ((HikariDataSource) dataSource).getDriverClassName();

            // adapt for h2
            if (driverClass.equals("org.h2.Driver")) {
                createH2Domain("JSONB");
            }

            Flyway flyway = Flyway.configure()
                    .dataSource(dataSource)
                    .locations(locations)
                    .table(tableName)
                    .baselineOnMigrate(true)
                    .load();

            flyway.migrate();
        }
    }

    private void createH2Domain(String ...domains){
        try (Connection conn = dataSource.getConnection()) {
            for (String domain : domains) {
                logger.info("Create Domain {} for H2", domain);
                String createJsonbDomain = "CREATE DOMAIN IF NOT EXISTS \"" + domain + "\" AS TEXT;";
                conn.createStatement().execute(createJsonbDomain);
            }
        } catch (SQLException e) {
            logger.error("Failed to establish connection.", e);
            throw new RuntimeException(e);
        }
    }
}
