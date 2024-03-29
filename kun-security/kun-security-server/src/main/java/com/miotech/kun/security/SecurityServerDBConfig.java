package com.miotech.kun.security;

import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.autoconfigure.session.JdbcSessionDataSourceInitializer;
import org.springframework.boot.autoconfigure.session.JdbcSessionProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.SpringProperties;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.core.StatementCreatorUtils;
import org.springframework.session.jdbc.config.annotation.SpringSessionDataSource;

import javax.sql.DataSource;

/**
 * @author: Jie Chen
 * @created: 2021/1/17
 */
@Configuration
@Slf4j
public class SecurityServerDBConfig implements InitializingBean {

    @Value("${spring.datasource.username}")
    String username;

    @Value("${spring.datasource.password}")
    String password;

    @Bean("primaryDataSourceProperties")
    @Primary
    @ConfigurationProperties("spring.datasource")
    public DataSourceProperties primaryDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean("primaryDataSource")
    @Primary
    @ConfigurationProperties("spring.datasource.configuration")
    DataSource getPrimaryDataSource() {
        log.info("Primary datasource username {} password {}", username, password);
        return primaryDataSourceProperties()
                .initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
    }

    @Bean("sessionDataSourceProperties")
    @ConfigurationProperties("spring.session.datasource")
    public DataSourceProperties sessionDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean("sessionDataSource")
    @SpringSessionDataSource
    @ConfigurationProperties("spring.session.datasource.configuration")
    DataSource getSessionDataSource() {
        return sessionDataSourceProperties()
                .initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
    }

    @Bean
    JdbcSessionDataSourceInitializer jdbcSessionDataSourceInitializer(@Qualifier("sessionDataSource") DataSource dataSource,
                                                                      ResourceLoader resourceLoader, JdbcSessionProperties properties) {
        return new JdbcSessionDataSourceInitializer(dataSource, resourceLoader, properties);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        SpringProperties.setProperty(StatementCreatorUtils.IGNORE_GETPARAMETERTYPE_PROPERTY_NAME, "true");
    }
}
