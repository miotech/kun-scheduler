package com.miotech.kun.datadiscovery.util;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

/**
 * @program: kun
 * @description: basicMapper
 * @author: zemin  huang
 * @create: 2022-02-21 13:17
 **/
public abstract class BasicMapper<T> implements RowMapper<T> {


    public static boolean isExistColumn(ResultSet rs, String columnName) {
        try {
            if (rs.findColumn(columnName) > 0) {
                return true;
            }
        } catch (SQLException e) {
            return false;
        }
        return false;
    }

    public static <T> T zeroToNull(ResultSet rs, String columnName, Class<T> tClass) {
        try {
            T object = rs.getObject(columnName, tClass);
            if (rs.wasNull()) {
                return null;
            }
            return object;
        } catch (SQLException e) {
            return null;
        }
    }

}
