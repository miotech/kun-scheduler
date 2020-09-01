package com.miotech.kun.datadiscovery;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Primary;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy;
import org.testcontainers.containers.wait.strategy.WaitStrategy;

import javax.sql.DataSource;
import java.time.Duration;

import static java.time.temporal.ChronoUnit.SECONDS;

/**
 * @author: Jie Chen
 * @created: 2020/7/6
 */
@TestConfiguration
public class BaseConfiguration {

    private volatile PostgreSQLContainer PostgreS;

    @Value("${testsuite.postgresImage}")
    private String postgresImageName;

    @Bean("dataDiscoveryDataSource")
    @Primary
    public DataSource testingDataSource() {
        if (PostgreS == null) {
            initializeDataSource();
        }
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(PostgreS.getJdbcUrl());
        hikariConfig.setUsername(PostgreS.getUsername());
        hikariConfig.setPassword(PostgreS.getPassword());
        hikariConfig.setDriverClassName(PostgreS.getDriverClassName());
        return new HikariDataSource(hikariConfig);
    }

    private void initializeDataSource() {
        WaitStrategy waitStrategy = new LogMessageWaitStrategy()
                .withRegEx(".*database system is ready to accept connections.*\\s")
                .withTimes(1)
                .withStartupTimeout(Duration.of(60, SECONDS));

        PostgreS = new PostgreSQLContainer<>(postgresImageName)
                .withDatabaseName("kun")
                .withUsername("postgres")
                .withPassword("Mi0ying2020");
        PostgreS.start();
    }
}
