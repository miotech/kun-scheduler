package com.miotech.kun.dataplatform.web.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

/**
 * @author: Jie Chen
 * @created: 2020/7/27
 */
@Configuration
@EnableTransactionManagement
public class DBConfig {
    @Autowired
    private Environment environment;

    @Primary
    @Bean(name = "dataPlatformPostgreSQLDataSource")
    public DataSource postgresqlDataSource() {
        DataSourceProperties dataSourceProperties = Binder.get(environment)
                .bind("spring.datasource.postgresql", DataSourceProperties.class).get();
        return dataSourceProperties
                .initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
    }

    @Primary
    @Bean(name = "postgresqlJdbcTemplate")
    public JdbcTemplate postgresqlJdbcTemplate(@Qualifier("dataPlatformPostgreSQLDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean(name = "dataPlatformNeo4jDataSource")
    public DataSource neo4jDataSource() {
        DataSourceProperties dataSourceProperties = Binder.get(environment)
                .bind("spring.datasource.neo4j", DataSourceProperties.class).get();
        return dataSourceProperties
                .initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
    }

    @Bean(name = "neo4jJdbcTemplate")
    public JdbcTemplate neo4jJdbcTemplate(@Qualifier("dataPlatformNeo4jDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

}
