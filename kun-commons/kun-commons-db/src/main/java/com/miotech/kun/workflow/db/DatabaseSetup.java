package com.miotech.kun.workflow.db;

import org.flywaydb.core.Flyway;

import javax.sql.DataSource;

public class DatabaseSetup {
    private final DataSource dataSource;
    private final String[] locations;

    public DatabaseSetup(DataSource dataSource, String... locations) {
        this.dataSource = dataSource;
        this.locations = locations;
    }

    public void start() {
        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .locations(locations)
                .load();

        flyway.migrate();
    }
}
