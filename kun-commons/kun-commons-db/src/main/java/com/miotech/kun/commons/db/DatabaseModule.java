package com.miotech.kun.commons.db;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.util.Properties;

public class DatabaseModule extends AbstractModule {
    @Singleton
    @Provides
    public DataSource createDataSource(Properties props) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(props.getProperty("datasource.jdbcUrl"));
        config.setUsername(props.getProperty("datasource.username"));
        config.setPassword(props.getProperty("datasource.password"));
        config.setDriverClassName(props.getProperty("datasource.driverClassName"));
        return new HikariDataSource(config);
    }
}
