package com.miotech.kun.commons.db.sql;

import com.google.common.collect.ImmutableList;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.StringJoiner;

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

    public static String and(Collection<String> list) {
        StringJoiner and = new StringJoiner(" AND ");
        list.forEach((s -> and.add(String.format("%s=?", s))));
        return and.toString();
    }

    public static String set(Collection<String> list) {
        StringJoiner and = new StringJoiner(",");
        list.forEach((s -> and.add(String.format("%s=?", s))));
        return and.toString();
    }
}
