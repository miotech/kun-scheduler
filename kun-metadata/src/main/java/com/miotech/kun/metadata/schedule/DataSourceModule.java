package com.miotech.kun.metadata.schedule;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;

public class DataSourceModule extends AbstractModule {

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
