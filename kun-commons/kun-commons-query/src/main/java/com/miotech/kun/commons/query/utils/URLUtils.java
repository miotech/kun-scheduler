package com.miotech.kun.commons.query.utils;

import com.miotech.kun.commons.query.datasource.DataSourceType;
import org.apache.commons.lang3.StringUtils;

/**
 * @author: Jie Chen
 * @created: 2020/7/17
 */
public class URLUtils {

    public static String getURLPostfix(String datasourceType,
                                       String database,
                                       String customUrlPostfix) {
        String finalDatabase = database;
        if (DataSourceType.PostgreSQL.name().equals(datasourceType)) {
            String[] splitStr = database.split("\\.");
            finalDatabase = splitStr[0] + "?currentSchema=" + splitStr[1];
        } else if (DataSourceType.AWS.name().equals(datasourceType)) {
            finalDatabase = "";
        }

        if (StringUtils.isNotEmpty(customUrlPostfix)) {
            finalDatabase += customUrlPostfix;
        }
        return finalDatabase;
    }
}
