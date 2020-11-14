package com.miotech.kun.commons.db.sql;

public class WhereClause {
    private final String preparedSQLSegment;

    private final Object[] params;

    public WhereClause(String preparedSQLSegment, Object[] params) {
        this.preparedSQLSegment = preparedSQLSegment;
        this.params = params;
    }

    public String getPreparedSQLSegment() {
        return preparedSQLSegment;
    }

    public Object[] getParams() {
        return params;
    }
}
