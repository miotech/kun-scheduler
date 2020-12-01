package com.miotech.kun.commons.db.sql;

import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.Collections;

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

    public static <T> String generateSqlInClausePlaceholders(Collection<T> paramCollection) {
        return String.join(", ", Collections.nCopies(paramCollection.size(), "?"));
    }
}
