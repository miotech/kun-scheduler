package com.miotech.kun.metadata.schedule;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;

public class DataSourceModule extends AbstractModule {

    private static String hostname;
    private static int port;
    private static String username;
    private static String password;
    private static String url;
    private static final String POSTGRES_DRIVER_CLASS = "org.postgresql.Driver";

    private final String resource;

    public DataSourceModule(String resource) {
        this.resource = resource;
        init();
    }

    private void init() {
        Config config = ConfigFactory.load(resource);
        hostname = config.getString("com.miotech.kun.data.collect.postgres.hostname");
        port = config.getInt("com.miotech.kun.data.collect.postgres.port");
        username = config.getString("com.miotech.kun.data.collect.postgres.username");
        password = config.getString("com.miotech.kun.data.collect.postgres.password");
        url = String.format("jdbc:postgresql://%s:%d/kun", hostname, port);
    }

    @Provides
    @Singleton
    public DataSource createDataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername(username);
        config.setPassword(password);
        config.setDriverClassName(POSTGRES_DRIVER_CLASS);
        return new HikariDataSource(config);
    }

}
