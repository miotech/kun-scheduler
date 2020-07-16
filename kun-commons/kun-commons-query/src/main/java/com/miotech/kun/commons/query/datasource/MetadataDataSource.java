package com.miotech.kun.commons.query.datasource;

import com.miotech.kun.commons.query.service.ConfigService;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.util.Properties;

/**
 * @author: Jie Chen
 * @created: 2020/7/13
 */
public class MetadataDataSource {

    private DataSource metadataDataSource;

    private MetadataDataSource() {
        initMetadataDataSource();
    }

    private static class SingletonHolder {
        private static MetadataDataSource instance = new MetadataDataSource();
    }

    public static MetadataDataSource getInstance() {
        return MetadataDataSource.SingletonHolder.instance;
    }

    public DataSource getMetadataDataSource() {
        return this.metadataDataSource;
    }

    private void initMetadataDataSource() {
        Properties props = ConfigService.getInstance().getProperties();
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(props.getProperty("metadata.datasource.url"));
        config.setUsername(props.getProperty("metadata.datasource.username"));
        config.setPassword(props.getProperty("metadata.datasource.password"));
        config.setDriverClassName(props.getProperty("metadata.datasource.driver-class-name"));
        this.metadataDataSource = new HikariDataSource(config);
    }
}
