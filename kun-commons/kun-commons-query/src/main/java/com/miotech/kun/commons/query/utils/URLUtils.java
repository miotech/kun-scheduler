package com.miotech.kun.commons.query.utils;

import com.miotech.kun.commons.query.datasource.DataSourceType;

/**
 * @author: Jie Chen
 * @created: 2020/7/17
 */
public class URLUtils {

    public static String getURLPostfix(String datasourceType,
                                       String database) {
        if (DataSourceType.PostgreSQL.name().equals(datasourceType)) {
            String[] splitStr = database.split("\\.");
            return splitStr[0] + "?currentSchema=" + splitStr[1];
        }
        if (DataSourceType.AWS.name().equals(datasourceType)) {
            return "";
        }
        return database;
    }
}
