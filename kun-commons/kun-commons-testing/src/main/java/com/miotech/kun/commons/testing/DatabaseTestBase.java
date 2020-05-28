package com.miotech.kun.commons.testing;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.inject.*;
import com.miotech.kun.workflow.db.DatabaseOperator;
import com.miotech.kun.workflow.db.DatabaseSetup;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public abstract class DatabaseTestBase extends GuiceTestBase {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseTestBase.class);

    @Inject
    private DataSource dataSource;

    private List<String> userTables;

    @Override
    protected void configuration() {
        super.configuration();
        addModules(new TestDatabaseModule());
    }

    @Before
    public void setUp() {
        // initialize database
        DatabaseSetup setup = new DatabaseSetup(dataSource, "sql/");
        setup.start();
    }

    @After
    public void tearDown() {
        truncateAllTables();
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
                if (tableName.startsWith("kun_")) {
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
            config.setJdbcUrl("jdbc:h2:mem:test;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE");
            config.setUsername("sa");
            config.setDriverClassName("org.h2.Driver");
            return new HikariDataSource(config);
        }
    }
}
