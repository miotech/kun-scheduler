package com.miotech.kun.security;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.autoconfigure.session.JdbcSessionDataSourceInitializer;
import org.springframework.boot.autoconfigure.session.JdbcSessionProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.session.jdbc.config.annotation.SpringSessionDataSource;

import javax.sql.DataSource;

@Configuration
public class SecurityDBConfig {

    @Bean(name = "securityDataSourceProperties")
    @ConfigurationProperties("security.datasource")
    public DataSourceProperties dataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean(name = "securityDataSource")
    @ConfigurationProperties(prefix = "security.datasource.configuration")
    @SpringSessionDataSource
    public DataSource dataSource() {
        return dataSourceProperties().initializeDataSourceBuilder().type(HikariDataSource.class).build();
    }

    @Bean
    JdbcSessionDataSourceInitializer jdbcSessionDataSourceInitializer(@Qualifier("securityDataSource") DataSource dataSource,
                                                                      ResourceLoader resourceLoader,
                                                                      JdbcSessionProperties properties) {
        return new JdbcSessionDataSourceInitializer(dataSource, resourceLoader, properties);
    }

    @Bean(name = "securityJdbcTemplate")
    public JdbcTemplate primaryJdbcTemplate(
            @Qualifier("securityDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}
