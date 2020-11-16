package com.miotech.kun.commons.db.sql;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

public enum SortOrder {
    ASC("ASC"),
    DESC("DESC");

    private final String sqlString;

    public final String getSqlString() {
        return this.sqlString;
    }

    SortOrder(String sqlString) {
        this.sqlString = sqlString.toUpperCase();
    }

    public static SortOrder from(String stringValue) {
        Preconditions.checkNotNull(stringValue);
        if (Objects.equal(stringValue.toUpperCase(), "ASC")) {
            return ASC;
        } else if (Objects.equal(stringValue.toUpperCase(), "DESC")) {
            return DESC;
        }
        // else
        throw new IllegalArgumentException(String.format("Invalid sort order: %s, expected to be \"ASC\" or \"DESC\"", stringValue));
    }
}
