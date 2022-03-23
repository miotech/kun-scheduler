package com.miotech.kun.commons.testing;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.miotech.kun.commons.db.DatabaseOperator;
import com.miotech.kun.commons.db.DatabaseSetup;
import com.miotech.kun.commons.utils.Props;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.PostgreSQLContainer;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static com.miotech.kun.commons.utils.CloseableUtils.closeIfPossible;

public abstract class DatabaseTestBase extends GuiceTestBase {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseTestBase.class);

    public static final String POSTGRES_IMAGE = "postgres:12.3";

    protected static PostgreSQLContainer postgres = startPostgres();

    private DataSource dataSource;

    private List<String> userTables;

    protected String flywayLocation;

    protected void setFlywayLocation(){
        flywayLocation = "kun-infra/";
    }

    protected List<String> ignoredTables() {
        return ImmutableList.of("kun_mt_datasource_type", "kun_mt_dataset_field_mapping");
    }

    @Override
    protected void configuration() {
        super.configuration();
        setFlywayLocation();
        addModules(new TestDatabaseModule());
    }

    @BeforeEach
    public void initDatabase() {
        // initialize database
        dataSource = injector.getInstance(DataSource.class);
        Props props = new Props();
        DatabaseSetup setup = new DatabaseSetup(dataSource, props, flywayLocation);
        setup.start();
    }

    @AfterEach
    public void tearDown() {
        // clear all tables
        truncateAllTables();

        // close datasource if necessary
        closeIfPossible(dataSource);
    }

    private static PostgreSQLContainer startPostgres() {
        PostgreSQLContainer postgres = new PostgreSQLContainer<>(POSTGRES_IMAGE);
        postgres.start();
        return postgres;
    }

    private void truncateAllTables() {
        DatabaseOperator operator = new DatabaseOperator(dataSource);
        for (String t : inferUserTables(dataSource)) {
            operator.update(String.format("TRUNCATE TABLE %s;", t));
        }
    }

    private List<String> inferUserTables(DataSource dataSource) {
        if (userTables != null) {
            return userTables;
        }

        try (Connection conn = dataSource.getConnection()) {
            List<String> tables = Lists.newArrayList();
            ResultSet rs = conn.getMetaData()
                    .getTables(null, null, "%", new String[]{"TABLE"});
            while (rs.next()) {
                String tableName = rs.getString(3);
                if (tableName.startsWith("kun_") && !ignoredTables().contains(tableName)) {
                    tables.add(tableName);
                }
            }
            userTables = ImmutableList.copyOf(tables);
            return userTables;
        } catch (SQLException e) {
            logger.error("Failed to establish connection.", e);
            throw new RuntimeException(e);
        }
    }

    public static class TestDatabaseModule extends AbstractModule {
        @Provides
        @Singleton
        public DataSource createDataSource() {
            HikariConfig config = new HikariConfig();
            config.setUsername(postgres.getUsername());
            config.setPassword(postgres.getPassword());
            config.setJdbcUrl(postgres.getJdbcUrl() + "&stringtype=unspecified");
            config.setDriverClassName("org.postgresql.Driver");
            return new HikariDataSource(config);
        }
    }
}
