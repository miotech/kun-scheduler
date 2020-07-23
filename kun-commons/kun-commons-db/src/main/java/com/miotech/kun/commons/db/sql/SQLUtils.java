package com.miotech.kun.commons.db.sql;

import org.apache.commons.lang3.StringUtils;

public class SQLUtils {
    private SQLUtils() {
    }

    public static String column(String column, String alias) {
        if (StringUtils.isEmpty(alias)) {
            return column;
        } else {
            return alias + "_" + column;
        }
    }
}
