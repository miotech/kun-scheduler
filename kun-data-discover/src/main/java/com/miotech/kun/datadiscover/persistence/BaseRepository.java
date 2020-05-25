package com.miotech.kun.datadiscover.persistence;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * @author: JieChen
 * @created: 6/12/20
 */
public abstract class BaseRepository {

    public String toLikeSql(String keyword) {
        return "%" + keyword + "%";
    }

    public String collectionToConditionSql(Collection<?> collection) {
        return collectionToConditionSql(null, collection);
    }

    public String collectionToConditionSql(List<Object> pstmtArgs, Collection<?> collection) {
        if (!CollectionUtils.isEmpty(collection)) {
            StringBuilder collectionSql = new StringBuilder("(");
            for (Object object : collection) {
                collectionSql.append("?").append(",");
                if (pstmtArgs != null) {
                    pstmtArgs.add(object);
                }
            }
            collectionSql.deleteCharAt(collectionSql.length() - 1);
            collectionSql.append(")");
            return collectionSql.toString();
        }
        return "";
    }

    public String collectionToConditionSql(String... vals) {
        return collectionToConditionSql(null, vals);
    }

    public String collectionToConditionSql(List<Object> pstmtArgs, String... vals) {
        if (ArrayUtils.isNotEmpty(vals)) {
            return collectionToConditionSql(pstmtArgs, Arrays.asList(vals));
        }
        return "";
    }

    public String toValuesSql(int valLength, int columnLength) {
        if (valLength == 0 || columnLength == 0) {
            return "";
        }
        StringBuilder stringBuilder = new StringBuilder();
        String columnSql = toColumnSql(columnLength);
        for (int i = 0; i < valLength; i++) {
            stringBuilder.append(columnSql).append(",");
        }
        stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        return stringBuilder.toString();
    }

    public String toColumnSql(int length) {
        if (length == 0) {
            return "";
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("(");
        for (int i = 0; i < length; i++) {
            stringBuilder.append("?").append(",");
        }
        stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        stringBuilder.append(")");
        return stringBuilder.toString();
    }

    public String pageInfoToSql(int pageNum, int pageSize) {
        StringBuilder pageSql = new StringBuilder();
        pageSql.append("limit ").append(pageSize).append(" offset ").append((pageNum - 1) * pageSize);
        return pageSql.toString();
    }

    public List<String> sqlToList(String sql) {
        if (StringUtils.isNotEmpty(sql)) {
            return Arrays.asList(sql.split(","));
        }
        return null;
    }
}
