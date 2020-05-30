package com.miotech.kun.metadata.client;

import com.google.inject.*;
import com.miotech.kun.metadata.schedule.DataBuilder;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.Before;
import org.junit.Test;

import javax.inject.Inject;
import javax.sql.DataSource;

public class DataBuilderTest {

    @Inject
    private DataBuilder builder;

    @Before
    public void setUp() {
        Injector injector = Guice.createInjector(new TestDatabaseModule());
        injector.injectMembers(this);
    }

    @Test
    public void testBuild() {
        builder.build(1L);
    }

    @Test
    public void testBuildAll() {
        builder.buildAll();
    }

    public static class TestDatabaseModule extends AbstractModule {
        @Provides
        @Singleton
        public DataSource createDataSource() {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl("jdbc:postgresql://10.0.1.114:5432/kun");
            config.setUsername("postgres");
            config.setPassword("Mi0ying2017");
            config.setDriverClassName("org.postgresql.Driver");
            return new HikariDataSource(config);
        }
    }

}
