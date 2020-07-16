package com.miotech.kun.commons.query.service;

import com.miotech.kun.commons.utils.PropertyUtils;

import java.util.Properties;

/**
 * @author: Jie Chen
 * @created: 2020/7/13
 */
public class ConfigService {

    private Properties properties;

    private ConfigService() {}

    private static class SingletonHolder {
        private static ConfigService instance = new ConfigService();
    }

    public static ConfigService getInstance() {
        return ConfigService.SingletonHolder.instance;
    }

    public void loadConf(String configName) {
        this.properties = PropertyUtils.loadAppProps(configName);
    }

    public Properties getProperties() {
        if (properties == null) {
            properties = PropertyUtils.loadAppProps();
        }
        return properties;
    }
}
