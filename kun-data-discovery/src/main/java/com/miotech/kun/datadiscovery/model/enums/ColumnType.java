package com.miotech.kun.datadiscovery.model.enums;


import com.miotech.kun.datadiscovery.util.DateFormatFactory;
import org.apache.commons.lang3.StringUtils;
import org.apache.spark.sql.types.DataType;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.DecimalType;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Locale;
import java.util.function.Predicate;

public enum ColumnType {
    INT(DataTypes.IntegerType, "int", ColumnType::testInt),
    BIGINT(DataTypes.LongType, "bigint", ColumnType::testBigint),
    FLOAT(DataTypes.FloatType, "float", ColumnType::testFloat),
    DOUBLE(DataTypes.DoubleType, "double", ColumnType::testDouble),
    DECIMAL(DecimalType.SYSTEM_DEFAULT(), "decimal", ColumnType::testDecimal),
    BOOLEAN(DataTypes.BooleanType, "boolean", ColumnType::testBoolean),
    STRING(DataTypes.StringType, "string", ColumnType::testString),
    TIMESTAMP(DataTypes.TimestampType, "timestamp", ColumnType::testTimestamp),
    DATE(DataTypes.DateType, "date", ColumnType::testDate);
    private DataType sparkDataType;
    private String simpleName;
    private Predicate<String> testFunction;

    ColumnType(DataType sparkDataType, String simpleName, Predicate<String> testFunction) {
        this.sparkDataType = sparkDataType;
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

    public static DataType dataType(String name) {
        return columnType(name).sparkDataType;
    }

    public DataType getSparkDataType() {
        return sparkDataType;
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
            DateFormatFactory.getFormat().parse(value);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public boolean test(String value) {

        return testFunction.test(value);
    }
}
