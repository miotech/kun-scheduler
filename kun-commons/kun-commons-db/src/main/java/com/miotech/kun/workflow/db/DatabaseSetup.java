package com.miotech.kun.workflow.db;

import com.zaxxer.hikari.HikariDataSource;
import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseSetup {
    private final Logger logger = LoggerFactory.getLogger(DatabaseSetup.class);

    private final DataSource dataSource;
    private final String[] locations;

    public DatabaseSetup(DataSource dataSource, String... locations) {
        this.dataSource = dataSource;
        this.locations = locations;
    }

    public void start() {
        String driverClass = ((HikariDataSource) dataSource).getDriverClassName();
        // adpat for h2
        if (driverClass.equals("org.h2.Driver")) {
            createH2Domain("JSONB");
        }
        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .locations(locations)
                .load();

        flyway.migrate();
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
