package com.miotech.kun.datadiscovery.model.enums;


import com.miotech.kun.common.utils.JSONUtils;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;

public enum ColumnType {
    INT("int", ColumnType::testInt),
    BIGINT("bigint", ColumnType::testBigint),
    FLOAT("float", ColumnType::testFloat),
    DOUBLE("double", ColumnType::testDouble),
    DECIMAL("decimal", ColumnType::testDecimal),
    BOOLEAN("boolean", ColumnType::testBoolean),
    STRING("string", ColumnType::testString),
    TIMESTAMP("timestamp", ColumnType::testTimestamp),
    DATE("date", ColumnType::testDate);

    private String simpleName;
    private Predicate<String> testFunction;

    ColumnType(String simpleName, Predicate<String> testFunction) {
        this.simpleName = simpleName;
        this.testFunction = testFunction;
    }

    public String getSimpleName() {
        return simpleName;
    }


    public static boolean existColumnType(String name) {
        try {
            ColumnType columnType = ColumnType.valueOf(name.toUpperCase(Locale.ROOT));
            return true;
        } catch (Exception e) {
            return false;
        }

    }

    public static ColumnType columnType(String name) {
        return ColumnType.valueOf(name.toUpperCase(Locale.ROOT));
    }

    private static boolean testInt(String value) {
        if (StringUtils.isBlank(value)) {
            return true;
        }
        try {
            Integer.parseInt(value);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    private static boolean testBigint(String value) {
        if (StringUtils.isBlank(value)) {
            return true;
        }
        try {
            Long.parseLong(value);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    private static boolean testFloat(String value) {
        if (StringUtils.isBlank(value)) {
            return true;
        }
        try {
            Float.parseFloat(value);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    private static boolean testDouble(String value) {
        if (StringUtils.isBlank(value)) {
            return true;
        }
        try {
            Double.parseDouble(value);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    private static boolean testDecimal(String value) {
        if (StringUtils.isBlank(value)) {
            return true;
        }
        try {
            BigDecimal bigDecimal = BigDecimal.valueOf(Double.parseDouble(value));
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    private static boolean testBoolean(String value) {
        if (StringUtils.isBlank(value)) {
            return true;
        }
        return value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false");
    }

    private static boolean testString(String value) {
        return true;
    }


    private static boolean testTimestamp(String value) {
        if (StringUtils.isBlank(value)) {
            return true;
        }
        try {
            Timestamp.valueOf(value);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    private static boolean testDate(String value) {
        if (StringUtils.isBlank(value)) {
            return true;
        }
        try {
            Date.valueOf(value);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public boolean test(String value) {

        return testFunction.test(value);
    }
}
