package com.miotech.kun.workflow.db;

import java.sql.ResultSet;
import java.sql.SQLException;

@FunctionalInterface
public interface ResultSetMapper<T> {
    public T map(ResultSet rs) throws SQLException;
}
