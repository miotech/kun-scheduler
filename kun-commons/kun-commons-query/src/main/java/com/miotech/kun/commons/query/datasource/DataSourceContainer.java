package com.miotech.kun.commons.query.datasource;

import com.miotech.kun.commons.query.QuerySite;
import com.miotech.kun.commons.query.model.JDBCConnectionInfo;
import com.miotech.kun.commons.query.model.MetadataConnectionInfo;
import com.miotech.kun.commons.query.service.MetadataService;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author: Jie Chen
 * @created: 2020/7/10
 */
public class DataSourceContainer {

    private static final String CACHE_ID_SEPARATOR = ":";

    private MetadataService metadataService;

    private final Object initDataSourceLock = new Object();

    private ConcurrentMap<String, DataSource> queryDataSourceCache;

    private DataSourceContainer() {
        metadataService = new MetadataService();
        queryDataSourceCache = new ConcurrentHashMap<>();
    }

    private static class SingletonHolder {
        private static DataSourceContainer instance = new DataSourceContainer();
    }

    public static DataSourceContainer getInstance() {
        return DataSourceContainer.SingletonHolder.instance;
    }

    private DataSource initDataSource(String url,
                                      String username,
                                      String password,
                                      String driverClassName) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername(username);
        config.setPassword(password);
        config.setDriverClassName(driverClassName);
        return new HikariDataSource(config);
    }

    public DataSource getCacheDataSource(QuerySite querySite) {
        DataSource dataSource = queryDataSourceCache.get(getCacheId(querySite));
        if (dataSource != null) {
            return dataSource;
        }
        synchronized (initDataSourceLock) {
            if (dataSource != null) {
                return dataSource;
            }
            MetadataConnectionInfo mcInfo = metadataService.getConnectionInfo(querySite);
            JDBCConnectionInfo jcInfo = DataSourceType.getJDBCConnectionInfo(mcInfo);
            dataSource = initDataSource(jcInfo.getUrl(),
                    jcInfo.getUsername(),
                    jcInfo.getPassword(),
                    jcInfo.getDriverClass());
            queryDataSourceCache.put(getCacheId(querySite), dataSource);
        }
        return dataSource;
    }

    private String getCacheId(QuerySite querySite) {
        return querySite.getSiteId()
                + CACHE_ID_SEPARATOR
                + querySite.getUrlPostfix();
    }

    public void cleanUp() {
        for (DataSource dataSource : queryDataSourceCache.values()) {
            ((HikariDataSource) dataSource).close();
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (DataSource dataSource : queryDataSourceCache.values()) {
            HikariDataSource hikariDataSource = ((HikariDataSource) dataSource);
            builder.append("url: ");
            builder.append(hikariDataSource.getJdbcUrl());
            builder.append("\n");
        }
        return builder.toString();
    }
}
