package com.miotech.kun.common;

import com.miotech.kun.common.utils.DateUtils;
import com.miotech.kun.common.utils.JSONUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author: Jie Chen
 * @created: 6/12/20
 */
public abstract class BaseRepository {

    public static final String COLUMN_PARENT_ID = "parent_id";

    public static final String DO_NOTHING = "do nothing";

    public String wrapSql(String sql) {
        return sql + "\n";
    }

    public int getOffset(int pageNumber, int pageSize) {
        return (pageNumber - 1) * pageSize;
    }

    public String toLikeSql(String keyword) {
        return "%" + keyword + "%";
    }

    public String collectionToConditionSql(Collection<?> collection) {
        return collectionToConditionSql(null, collection);
    }

    public String collectionToConditionSql(List<Object> pstmtArgs, Collection<?> collection) {
        if (CollectionUtils.isNotEmpty(collection)) {
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

    public String toValuesSql(int rowLength, int columnLength) {
        if (rowLength == 0 || columnLength == 0) {
            return "";
        }
        StringJoiner stringJoiner = new StringJoiner(",");
        String columnSql = toColumnSql(columnLength);
        for (int i = 0; i < rowLength; i++) {
            stringJoiner.add(columnSql);
        }
        return stringJoiner.toString();
    }

    public String toColumnSql(int length) {
        if (length == 0) {
            return "";
        }
        StringJoiner stringJoiner = new StringJoiner(",", "(", ")");
        for (int i = 0; i < length; i++) {
            stringJoiner.add("?");
        }
        return stringJoiner.toString();
    }

    public String toLimitSql(int pageNum, int pageSize) {
        StringBuilder pageSql = new StringBuilder();
        pageSql.append("limit ").append(pageSize).append(" offset ").append((pageNum - 1) * pageSize);
        return pageSql.toString();
    }

    public List<String> sqlToList(String sql) {
        if (StringUtils.isNotEmpty(sql)) {
            return Arrays.asList(sql.split(","));
        }
        return Collections.emptyList();
    }

    public Long timestampToMillis(ResultSet rs, String columnLabel) throws SQLException {
        return DateUtils.dateTimeToMillis(rs.getObject(columnLabel, OffsetDateTime.class));
    }

    public LocalDateTime millisToTimestamp(Long millis) {
        return ObjectUtils.defaultIfNull(DateUtils.millisToLocalDateTime(millis), null);
    }

    public List<Map> convertQueryListToQueryMaps(List<?> list) {
        return list.stream().map(JSONUtils::toJsonObject).collect(Collectors.toList());
    }

}
