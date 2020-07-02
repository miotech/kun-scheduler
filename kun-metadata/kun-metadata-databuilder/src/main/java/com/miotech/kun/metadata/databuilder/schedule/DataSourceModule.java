package com.miotech.kun.metadata.databuilder.schedule;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.util.Properties;

public class DataSourceModule extends AbstractModule {

    private String username;
    private String password;
    private String url;
    private Properties props;

    public DataSourceModule(Properties props) {
        this.props = props;
        init();
    }

    private void init() {
        url = props.getProperty("datasource.jdbcUrl");
        username = props.getProperty("datasource.username");
        password = props.getProperty("datasource.password");
    }

    @Provides
    @Singleton
    public DataSource createDataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername(username);
        config.setPassword(password);
        config.setDriverClassName(props.getProperty("datasource.driverClassName"));
        return new HikariDataSource(config);
    }

}
