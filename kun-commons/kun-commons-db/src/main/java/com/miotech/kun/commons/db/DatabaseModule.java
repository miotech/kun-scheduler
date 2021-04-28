package com.miotech.kun.commons.db;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.miotech.kun.commons.utils.Props;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;

public class DatabaseModule extends AbstractModule {
    @Singleton
    @Provides
    public DataSource createDataSource(Props props) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(props.get("datasource.jdbcUrl"));
        config.setUsername(props.get("datasource.username"));
        config.setPassword(props.get("datasource.password"));
        config.setDriverClassName(props.get("datasource.driverClassName"));

        DataSource ds = new HikariDataSource(config);
        DatabaseSetup databaseSetup = new DatabaseSetup(ds, props);
        databaseSetup.start();
        return ds;
    }
}
