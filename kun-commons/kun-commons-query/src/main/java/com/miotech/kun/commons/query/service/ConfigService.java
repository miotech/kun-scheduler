package com.miotech.kun.commons.query.service;

import com.miotech.kun.commons.utils.Props;
import com.miotech.kun.commons.utils.PropsUtils;

/**
 * @author: Jie Chen
 * @created: 2020/7/13
 */
public class ConfigService {

    private Props props;

    private ConfigService() {
        props = Props.fromProperties(System.getProperties());
    }

    private static class SingletonHolder {
        private static ConfigService instance = new ConfigService();
    }

    public static ConfigService getInstance() {
        return ConfigService.SingletonHolder.instance;
    }

    public void loadConf(String configName) {
        this.props = PropsUtils.loadAppProps(configName);
    }

    public void loadConf() {
        this.props = PropsUtils.loadAppProps();
    }

    public Props getProperties() {
        return props;
    }

    public void setMetadataDataSourceUrl(String url) {
        this.props.put("metadata.datasource.url", url);
    }

    public void setMetadataDataSourceUsername(String username) {
        this.props.put("metadata.datasource.username", username);
    }

    public void setMetadataDataSourcePassword(String password) {
        this.props.put("metadata.datasource.password", password);
    }

    public void setMetadataDataSourceDriverClass(String driverClass) {
        this.props.put("metadata.datasource.driver-class-name", driverClass);
    }
}
