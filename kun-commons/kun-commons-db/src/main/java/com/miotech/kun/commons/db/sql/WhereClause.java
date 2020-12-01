package com.miotech.kun.commons.db.sql;

public class WhereClause {
    private final String sqlSegment;

    private final Object[] params;

    public WhereClause(String sqlSegment, Object[] params) {
        this.sqlSegment = sqlSegment;
        this.params = params;
    }

    public String getSqlSegment() {
        return sqlSegment;
    }

    public Object[] getParams() {
        return params;
    }
}
