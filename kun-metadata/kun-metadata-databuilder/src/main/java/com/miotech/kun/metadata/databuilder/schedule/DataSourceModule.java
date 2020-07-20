package com.miotech.kun.metadata.databuilder.schedule;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class DataSourceModule extends AbstractModule {

    private final Properties props;

    public DataSourceModule(Properties props) {
        this.props = props;
    }

    @Provides
    @Singleton
    public DataSource createDataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(props.getProperty("datasource.jdbcUrl"));
        config.setUsername(props.getProperty("datasource.username"));
        config.setPassword(props.getProperty("datasource.password"));
        config.setDriverClassName(props.getProperty("datasource.driverClassName"));
        config.setMaximumPoolSize(2);
        config.setMinimumIdle(0);
        config.setIdleTimeout(TimeUnit.SECONDS.toMillis(10));
        return new HikariDataSource(config);
    }

}
