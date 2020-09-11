package com.miotech.kun.commons.query.datasource;

import com.miotech.kun.commons.query.service.ConfigService;
import com.miotech.kun.commons.utils.Props;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.commons.lang3.StringUtils;

import javax.sql.DataSource;
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
        Props props = ConfigService.getInstance().getProperties();
        if (StringUtils.isEmpty(props.get("metadata.datasource.url"))) {
            ConfigService.getInstance().loadConf();
            props = ConfigService.getInstance().getProperties();
        }
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(props.get("metadata.datasource.url"));
        config.setUsername(props.get("metadata.datasource.username"));
        config.setPassword(props.get("metadata.datasource.password"));
        config.setDriverClassName(props.get("metadata.datasource.driver-class-name"));
        this.metadataDataSource = new HikariDataSource(config);
    }

    public void cleanUp() {
        if (metadataDataSource != null) {
            ((HikariDataSource) metadataDataSource).close();
        }
    }
}
