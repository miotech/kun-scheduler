package com.miotech.kun.dataplatform.web.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
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

    @Bean(name="dataPlatformPostgreSQLDataSourceProperties")
    @Primary
    @ConfigurationProperties("spring.datasource.postgresql")
    public DataSourceProperties postgresqlDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean(name = "dataPlatformPostgreSQLDataSource")
    @Primary
    @ConfigurationProperties("spring.datasource.hikari")
    public DataSource postgresqlDataSource(@Qualifier("dataPlatformPostgreSQLDataSourceProperties") DataSourceProperties dataSourceProperties) {
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

    @Bean(name="dataPlatformNeo4jDataSourceProperties")
    @ConfigurationProperties("spring.datasource.neo4j")
    public DataSourceProperties neo4jDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean(name = "dataPlatformNeo4jDataSource")
    @ConditionalOnExpression("${spring.datasource.neo4j.enabled:true}")
    public DataSource neo4jDataSource(@Qualifier("dataPlatformNeo4jDataSourceProperties") DataSourceProperties dataSourceProperties) {
        return dataSourceProperties
                .initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
    }

    @Bean(name = "neo4jJdbcTemplate")
    @ConditionalOnExpression("${spring.datasource.neo4j.enabled:true}")
    public JdbcTemplate neo4jJdbcTemplate(@Qualifier("dataPlatformNeo4jDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

}
