package com.miotech.kun.commons.query.service;

import com.miotech.kun.commons.utils.PropertyUtils;

import java.util.Properties;

/**
 * @author: Jie Chen
 * @created: 2020/7/13
 */
public class ConfigService {

    private Properties properties;

    private ConfigService() {
        properties = System.getProperties();
    }

    private static class SingletonHolder {
        private static ConfigService instance = new ConfigService();
    }

    public static ConfigService getInstance() {
        return ConfigService.SingletonHolder.instance;
    }

    public void loadConf(String configName) {
        this.properties = PropertyUtils.loadAppProps(configName);
    }

    public void loadConf() {
        this.properties = PropertyUtils.loadAppProps();
    }

    public Properties getProperties() {
        return properties;
    }

    public void setMetadataDataSourceUrl(String url) {
        this.properties.setProperty("metadata.datasource.url", url);
    }

    public void setMetadataDataSourceUsername(String username) {
        this.properties.setProperty("metadata.datasource.username", username);
    }

    public void setMetadataDataSourcePassword(String password) {
        this.properties.setProperty("metadata.datasource.password", password);
    }

    public void setMetadataDataSourceDriverClass(String driverClass) {
        this.properties.setProperty("metadata.datasource.driver-class-name", driverClass);
    }
}
